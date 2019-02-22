package com.okta.tools.authentication;

import com.amazonaws.util.IOUtils;
import com.okta.tools.OktaAwsCliEnvironment;
import com.okta.tools.helpers.CookieHelper;
import com.okta.tools.util.NodeListIterable;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public final class BrowserAuthentication extends Application {
    // Trade-off: JavaFX app model makes interacting with UI state challenging
    // Experienced JavaFX devs welcomed to suggest solutions to this
    private static final CountDownLatch USER_AUTH_COMPLETE = new CountDownLatch(1);

    // Trade-off: JavaFX app model makes passing parameters to UI challenging
    // Experienced JavaFX devs welcomed to suggest solutions to this
    private static OktaAwsCliEnvironment ENVIRONMENT;
    private static CookieHelper cookieHelper;

    // The value of samlResponse is only valid if USER_AUTH_COMPLETE has counted down to zero
    private static final AtomicReference<String> samlResponse = new AtomicReference<>();

    public static String login(OktaAwsCliEnvironment environment) throws InterruptedException {
        ENVIRONMENT = environment;
        cookieHelper = new CookieHelper(ENVIRONMENT);
        launch();
        USER_AUTH_COMPLETE.await();
        return samlResponse.get();
    }

    @Override
    public void start(final Stage stage) throws IOException {
        stage.setWidth(802);
        stage.setHeight(650);
        stage.setOnCloseRequest(event -> System.exit(1));
        Scene scene = new Scene(new Group());

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);

        URI uri = URI.create(ENVIRONMENT.oktaAwsAppUrl);
        initializeCookies(uri);

        registerCustomProtocolHandler();
        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
            System.out.println("JSConsoleListener: " + message + "[at " + lineNumber + "]");
        });

        webEngine.getLoadWorker().stateProperty()
                .addListener((ov, oldState, newState) -> {
                    if (webEngine.getDocument() != null) {
                        checkForAwsSamlSignon(stage, webEngine);
                        stage.setTitle(webEngine.getLocation());
                    }
                });

        webEngine.getLoadWorker().exceptionProperty()
            .addListener((ov, oldState, newState) -> {
                System.out.format("exception(%s => %s)\n%s\n", oldState, newState, webEngine.getLoadWorker().getException());
            });

        webEngine.load(uri.toASCIIString());

        scene.setRoot(scrollPane);

        stage.setScene(scene);
        stage.show();
    }

    private void initializeCookies(URI uri) throws IOException {
        Map<String, List<String>> headers = cookieHelper.loadCookieHeaders();
        java.net.CookieHandler.setDefault(new CookieManager(cookieHelper));
        java.net.CookieHandler.getDefault().put(uri, headers);
    }

    private void checkForAwsSamlSignon(Stage stage, WebEngine webEngine) {
        String samlResponseForAws = getSamlResponseForAws(webEngine.getDocument());
        if (samlResponseForAws != null) {
            finishAuthentication(stage, samlResponseForAws);
        }
    }

    private String getSamlResponseForAws(Document document) {
        Node awsStsSamlForm = getAwsStsSamlForm(document);
        if (awsStsSamlForm == null) return null;
        return getSamlResponseFromForm(awsStsSamlForm);
    }

    private Node getAwsStsSamlForm(Document document) {
        NodeList formNodes = document.getElementsByTagName("form");
        for (Node form : new NodeListIterable(formNodes)) {
            NamedNodeMap formAttributes = form.getAttributes();
            if (formAttributes == null) continue;
            Node formActionAttribute = formAttributes.getNamedItem("action");
            if (formActionAttribute == null) continue;
            String formAction = formActionAttribute.getTextContent();
            if ("https://signin.aws.amazon.com/saml".equals(formAction)) {
                return form;
            }
        }
        return null;
    }

    private String getSamlResponseFromForm(@Nonnull Node awsStsSamlForm) {
        Node samlResponseInput = getSamlResponseInput(awsStsSamlForm);
        if (samlResponseInput == null)
            throw new IllegalStateException("Request to AWS STS SAML endpoint missing SAMLResponse");
        NamedNodeMap attributes = samlResponseInput.getAttributes();
        Node value = attributes.getNamedItem("value");
        return value.getTextContent();
    }

    private Node getSamlResponseInput(@Nonnull Node parent) {
        for (Node child : new NodeListIterable(parent.getChildNodes())) {
            if (isSamlResponseInput(child)) {
                return child;
            } else {
                Node samlResponseInput = getSamlResponseInput(child);
                if (samlResponseInput != null) return samlResponseInput;
            }
        }
        return null;
    }

    private boolean isSamlResponseInput(@Nonnull Node child) {
        boolean isInput = "input".equals(child.getLocalName());
        if (!isInput) return false;
        NamedNodeMap attributes = child.getAttributes();
        if (attributes == null) return false;
        Node nameAttribute = attributes.getNamedItem("name");
        if (nameAttribute == null) return false;
        String name = nameAttribute.getTextContent();
        return "SAMLResponse".equals(name);
    }

    private void finishAuthentication(Stage stage, String samlResponseForAws) {
        samlResponse.set(samlResponseForAws);
        stage.close();
        USER_AUTH_COMPLETE.countDown();
    }

    /** https://stackoverflow.com/questions/52572853/failed-integrity-metadata-check-in-javafx-webview-ignores-systemprop
    *
    * javaFX.WebEngine with >1.8.0._162 cannot handle "integrity=" (attribute &lt;link&gt; or &lt;script&gt;) checks on files retrievals properly.
    * This custom stream handler will disable the integrity checks by replacing "integrity=" and "integrity =" with a "integrity.disabled" counterpart
    * This is very susceptible to breaking if Okta changes the response body again as we are making changes based on the format of the characters in their response
    */
    private void registerCustomProtocolHandler() {
        try {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                @Override
                public URLStreamHandler createURLStreamHandler(String protocol) {
                    if ("https".equals(protocol)) {
                        return new sun.net.www.protocol.https.Handler() {
                            @Override
                            protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
                                // Leaving sysout comments here for debugging and clarity if the tool does break again because of an Okta Update
                                //System.out.format("openConnection %s (%s, %s)\n", url, url.getHost(), url.getPath());

                                final HttpsURLConnectionImpl httpsURLConnection = (HttpsURLConnectionImpl) super.openConnection(url, proxy);
                                if ("artisan.okta.com".equals(url.getHost()) && "/home/amazon_aws/0oad7khqw5gSO701p0x7/272".equals(url.getPath())) {

                                    return new URLConnection(url) {
                                        @Override
                                        public void connect() throws IOException {
                                            httpsURLConnection.connect();
                                        }

                                        public InputStream getInputStream() throws IOException {
                                            byte[] content = IOUtils.toByteArray(httpsURLConnection.getInputStream());
                                            String contentAsString = new String(content, "UTF-8");

                                            //System.out.println("########################## got stream content ##############################");
                                            //System.out.println(contentAsString);
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            baos.write(contentAsString.replaceAll("integrity ?=", "integrity.disabled=").getBytes("UTF-8"));
                                            return new ByteArrayInputStream(baos.toByteArray());
                                        }

                                        public OutputStream getOutputStream() throws IOException {
                                            return httpsURLConnection.getOutputStream();
                                        }

                                    };

                                } else {
                                    return httpsURLConnection;
                                }
                            }

                        };
                    }
                    return null;
                }
            });
        } catch (Throwable t) {
            System.out.println("Unable to register custom protocol handler");
        }
    }
}
