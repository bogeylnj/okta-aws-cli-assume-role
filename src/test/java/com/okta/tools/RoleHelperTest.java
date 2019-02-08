package com.okta.tools;

import com.okta.tools.helpers.RoleHelper;
import com.okta.tools.models.AccountOption;
import com.okta.tools.models.RoleOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleHelperTest {

    private static final String EXAMPLE_SAML_RESPONSE = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpSZXNwb25zZSB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgRGVzdGluYXRpb249Imh0dHBzOi8vc2lnbmluLmF3cy5hbWF6b24uY29tL3NhbWwiCiAgICAgICAgICAgICAgICAgSUQ9ImlkMTE5ODc4MjAwNjQ1Nzg1NzYxNTUyODIwNjExIiBJc3N1ZUluc3RhbnQ9IjIwMTktMDEtMDhUMTM6MDI6MDMuMDIwWiIgVmVyc2lvbj0iMi4wIgogICAgICAgICAgICAgICAgIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSI+CiAgICA8c2FtbDI6SXNzdWVyIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIgogICAgICAgICAgICAgICAgICBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+aHR0cDovL3d3dy5va3RhLmNvbS9leGs1NTYxN3B1MHRIU3lZNDF0NwogICAgPC9zYW1sMjpJc3N1ZXI+CiAgICA8ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj4KICAgICAgICA8ZHM6U2lnbmVkSW5mbz4KICAgICAgICAgICAgPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgICAgICAgICAgPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz4KICAgICAgICAgICAgPGRzOlJlZmVyZW5jZSBVUkk9IiNpZDExOTg3ODIwMDY0NTc4NTc2MTU1MjgyMDYxMSI+CiAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3Jtcz4KICAgICAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz4KICAgICAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj4KICAgICAgICAgICAgICAgICAgICAgICAgPGVjOkluY2x1c2l2ZU5hbWVzcGFjZXMgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgUHJlZml4TGlzdD0ieHMiLz4KICAgICAgICAgICAgICAgICAgICA8L2RzOlRyYW5zZm9ybT4KICAgICAgICAgICAgICAgIDwvZHM6VHJhbnNmb3Jtcz4KICAgICAgICAgICAgICAgIDxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz4KICAgICAgICAgICAgICAgIDxkczpEaWdlc3RWYWx1ZT5Da0tyWElwNVVHWlN0RUQrQjI4RXhOUkZZM2tVOUkzRGllVU5aT0tHRHA0PTwvZHM6RGlnZXN0VmFsdWU+CiAgICAgICAgICAgIDwvZHM6UmVmZXJlbmNlPgogICAgICAgIDwvZHM6U2lnbmVkSW5mbz4KICAgICAgICA8ZHM6U2lnbmF0dXJlVmFsdWU+CiAgICAgICAgICAgIE0xSmhkOVRxNkhoRFFHSlNnc2NWMTdlTlVLTXpDb0VzYXhSZzhOeXhUclFBSmF2WWpKNHVnaDMxM2ZPTHRjU29XNlBsSVZsbmtVdU5nS2xqamVVcEd6WE5WdUlxT0RiRnVMY1paSFFvckpVVkk2RDB0d1ovZVBEYmNxaUU1QzBaZzgzQ2ErM2UzcFdOSDMwZzFGTkh1ZkVhdFVPd05vUWZqVHY4UVNGMFc4MkNoc0JlQlh5SU1rbWFBcGNEcmhBNk10QXZzREhhQVlqcFFNdGVadGJ1eDh1RWRYSUNBU0NycnA1dlJHTlVxbVQveWRYRlE2YkV5dERJV28vVUFSRWU2WjF3Rys2bHpHUHh2RGV4YzlRWFhDbXRLMkYvZlBKUk5mMU91TnFVSDgrUmQ2eU5MQTZMdHFDdjVlQWovR1ArSEdnVFdkREFUZk96c3lpamZORHVMdz09CiAgICAgICAgPC9kczpTaWduYXR1cmVWYWx1ZT4KICAgICAgICA8ZHM6S2V5SW5mbz4KICAgICAgICAgICAgPGRzOlg1MDlEYXRhPgogICAgICAgICAgICAgICAgPGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlEbmpDQ0FvYWdBd0lCQWdJR0FWbE5IUXBMTUEwR0NTcUdTSWIzRFFFQkJRVUFNSUdQTVFzd0NRWURWUVFHRXdKVlV6RVRNQkVHCiAgICAgICAgICAgICAgICAgICAgQTFVRUNBd0tRMkZzYVdadmNtNXBZVEVXTUJRR0ExVUVCd3dOVTJGdUlFWnlZVzVqYVhOamJ6RU5NQXNHQTFVRUNnd0VUMnQwWVRFVQogICAgICAgICAgICAgICAgICAgIE1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFREFPQmdOVkJBTU1CM1psY21GbWFXNHhIREFhQmdrcWhraUc5dzBCQ1FFV0RXbHUKICAgICAgICAgICAgICAgICAgICBabTlBYjJ0MFlTNWpiMjB3SGhjTk1UWXhNak13TURBeU16STFXaGNOTWpZeE1qTXdNREF5TkRJMVdqQ0JqekVMTUFrR0ExVUVCaE1DCiAgICAgICAgICAgICAgICAgICAgVlZNeEV6QVJCZ05WQkFnTUNrTmhiR2xtYjNKdWFXRXhGakFVQmdOVkJBY01EVk5oYmlCR2NtRnVZMmx6WTI4eERUQUxCZ05WQkFvTQogICAgICAgICAgICAgICAgICAgIEJFOXJkR0V4RkRBU0JnTlZCQXNNQzFOVFQxQnliM1pwWkdWeU1SQXdEZ1lEVlFRRERBZDJaWEpoWm1sdU1Sd3dHZ1lKS29aSWh2Y04KICAgICAgICAgICAgICAgICAgICBBUWtCRmcxcGJtWnZRRzlyZEdFdVkyOXRNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQWtNZk9XaURFCiAgICAgICAgICAgICAgICAgICAgaytObkZ6aStHRDFtcTArZ3lMZkV5MGx3WEhxSGpBV21VRDBmQktJNU1LTGpoUzBnbkVEWER2SDRQQ3VtZmoxNkZnTHQ0UHFZUVRSYwogICAgICAgICAgICAgICAgICAgIHkvYW9DSE9TY3U4UnBDUEtyYXFwWGZDWU9qeDZIWTRvYmZKMTA3eTl2L0VhUnVRYlBrc1Y1VHo5WTRKY2tLRUN3UCtManh5amhJYlUKICAgICAgICAgICAgICAgICAgICAvcVUyd2RWeHJ1Q3ZGVzEydkNUWmpOaEZHaHhWbUt2Q2FTSW5Cc1dET2l5MnJaNEcvTkVqSEthTUVtMjQwWDVBb0dtdVpMUHlhNnFUCiAgICAgICAgICAgICAgICAgICAgZm10Nm9pMEF2TmtBaWVndGExdUc4cjJPdjhScld5cDRwWkNJK2pQS2tNa2VxK2RrcWVYalFOU1BTQ2RwcXh5Sm5oNGtEbjZhMHhRTwogICAgICAgICAgICAgICAgICAgIHNsSG1HNnhsbUI0c0ZvalBWUDFLS2ZIc3BxTWVvUUlEQVFBQk1BMEdDU3FHU0liM0RRRUJCUVVBQTRJQkFRQmx2UlVoTFlhL0V1UTYKICAgICAgICAgICAgICAgICAgICA5bWJkd3lXVG9Vek5ySnFob1huN3NpYmJyR3NjMU9kZEpVY2dCRUE1VXNxSHpHMmJGazlSeHk3enhaSWxvdUZDL0cwcks4VndjelNDCiAgICAgICAgICAgICAgICAgICAgNERjVVZ3bVRaK0ZINnY4TDNSaUtmUkVDUzNORkNqREc4ZTcxY2x4RWNPaFp0Mm00NzhXYXZDV2FmNFdxRGZxKzlBd3JQaTlKbVB1dwogICAgICAgICAgICAgICAgICAgIG9GNkE5a2p3Y1BuWHM1ZWk0UC9CQWdGUVFPOEYxS0pVNUk5SWUwSjZ2L05Icy90QkExSXN2TzFPNDBHbmxzSmt4Y1QvbGhRZnVYc0MKICAgICAgICAgICAgICAgICAgICBYVEswdUprWWJDMkZLOHVDM1lWUEY2amo4cW05Q3Vlb29JRjJsTW56ekY3RCtLYlEzUy9HYXQybTFpZGU2ejBNcis4TXAvTjN0eTJHCiAgICAgICAgICAgICAgICAgICAgdlJWU29PaFJ0dUVvOWUxdGdVcmc3RXpYCiAgICAgICAgICAgICAgICA8L2RzOlg1MDlDZXJ0aWZpY2F0ZT4KICAgICAgICAgICAgPC9kczpYNTA5RGF0YT4KICAgICAgICA8L2RzOktleUluZm8+CiAgICA8L2RzOlNpZ25hdHVyZT4KICAgIDxzYW1sMnA6U3RhdHVzIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj4KICAgICAgICA8c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPgogICAgPC9zYW1sMnA6U3RhdHVzPgogICAgPHNhbWwyOkFzc2VydGlvbiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgSUQ9ImlkMTE5ODc4MjAwNjQ2NTgzMzUxNTY4ODI5Nzk2IgogICAgICAgICAgICAgICAgICAgICBJc3N1ZUluc3RhbnQ9IjIwMTktMDEtMDhUMTM6MDI6MDMuMDIwWiIgVmVyc2lvbj0iMi4wIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiPgogICAgICAgIDxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDplbnRpdHkiCiAgICAgICAgICAgICAgICAgICAgICB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL3d3dy5va3RhLmNvbS9leGs1NTYxN3B1MHRIU3lZNDF0NwogICAgICAgIDwvc2FtbDI6SXNzdWVyPgogICAgICAgIDxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgogICAgICAgICAgICA8ZHM6U2lnbmVkSW5mbz4KICAgICAgICAgICAgICAgIDxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CiAgICAgICAgICAgICAgICA8ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTI1NiIvPgogICAgICAgICAgICAgICAgPGRzOlJlZmVyZW5jZSBVUkk9IiNpZDExOTg3ODIwMDY0NjU4MzM1MTU2ODgyOTc5NiI+CiAgICAgICAgICAgICAgICAgICAgPGRzOlRyYW5zZm9ybXM+CiAgICAgICAgICAgICAgICAgICAgICAgIDxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPgogICAgICAgICAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj4KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxlYzpJbmNsdXNpdmVOYW1lc3BhY2VzIHhtbG5zOmVjPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiIFByZWZpeExpc3Q9InhzIi8+CiAgICAgICAgICAgICAgICAgICAgICAgIDwvZHM6VHJhbnNmb3JtPgogICAgICAgICAgICAgICAgICAgIDwvZHM6VHJhbnNmb3Jtcz4KICAgICAgICAgICAgICAgICAgICA8ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+CiAgICAgICAgICAgICAgICAgICAgPGRzOkRpZ2VzdFZhbHVlPnNjVklNNVBwUnovSFNHRHFOd3MrTyttdlVhZm9sSlhSY1lQRUJiOUhPRXc9PC9kczpEaWdlc3RWYWx1ZT4KICAgICAgICAgICAgICAgIDwvZHM6UmVmZXJlbmNlPgogICAgICAgICAgICA8L2RzOlNpZ25lZEluZm8+CiAgICAgICAgICAgIDxkczpTaWduYXR1cmVWYWx1ZT4KICAgICAgICAgICAgICAgIEdteW96REhxSHFKajNvTTRhRWZ2WVhvWFFHL2dOMU1uSkxTOHkwQ1JxNzBCZk9DbEo1emk4a0cveW5zVi92REw5RHhoUVAzaUk1WlpMb0w0YVlVcjdGc3R1bmdwV2Q5NDFCWm9veTlQMkNOT2F4NlIrNlJta2lvdmFSTmltbGcwNjkzeWwzMlpqVytIZFJjRkNCY203K2lFYkZTTEZiUExzdWd1YjlUR2I4SU1icVIrV3dFUFVubmx0K21YUGc3Q28wa2ROajJiKzhzVVRnYjRXbXlHRGFycXV6OHlkTGZaOTRBQUh1cjBiK1JHclV6ejhteGlBUmprWFpUTmJHNjMveXFoMEZqK0lGMXhNaTZleGxucnhQZ0dhNGNtUTJueU4rSTQ0N0Z4ZlN3RjJhUk1uL1FLcUpwTFpDUEEzU1dkcHF2TDhWMW41cnJmN3VEUU1HS2daZz09CiAgICAgICAgICAgIDwvZHM6U2lnbmF0dXJlVmFsdWU+CiAgICAgICAgICAgIDxkczpLZXlJbmZvPgogICAgICAgICAgICAgICAgPGRzOlg1MDlEYXRhPgogICAgICAgICAgICAgICAgICAgIDxkczpYNTA5Q2VydGlmaWNhdGU+TUlJRG5qQ0NBb2FnQXdJQkFnSUdBVmxOSFFwTE1BMEdDU3FHU0liM0RRRUJCUVVBTUlHUE1Rc3dDUVlEVlFRR0V3SlZVekVUTUJFRwogICAgICAgICAgICAgICAgICAgICAgICBBMVVFQ0F3S1EyRnNhV1p2Y201cFlURVdNQlFHQTFVRUJ3d05VMkZ1SUVaeVlXNWphWE5qYnpFTk1Bc0dBMVVFQ2d3RVQydDBZVEVVCiAgICAgICAgICAgICAgICAgICAgICAgIE1CSUdBMVVFQ3d3TFUxTlBVSEp2ZG1sa1pYSXhFREFPQmdOVkJBTU1CM1psY21GbWFXNHhIREFhQmdrcWhraUc5dzBCQ1FFV0RXbHUKICAgICAgICAgICAgICAgICAgICAgICAgWm05QWIydDBZUzVqYjIwd0hoY05NVFl4TWpNd01EQXlNekkxV2hjTk1qWXhNak13TURBeU5ESTFXakNCanpFTE1Ba0dBMVVFQmhNQwogICAgICAgICAgICAgICAgICAgICAgICBWVk14RXpBUkJnTlZCQWdNQ2tOaGJHbG1iM0p1YVdFeEZqQVVCZ05WQkFjTURWTmhiaUJHY21GdVkybHpZMjh4RFRBTEJnTlZCQW9NCiAgICAgICAgICAgICAgICAgICAgICAgIEJFOXJkR0V4RkRBU0JnTlZCQXNNQzFOVFQxQnliM1pwWkdWeU1SQXdEZ1lEVlFRRERBZDJaWEpoWm1sdU1Sd3dHZ1lKS29aSWh2Y04KICAgICAgICAgICAgICAgICAgICAgICAgQVFrQkZnMXBibVp2UUc5cmRHRXVZMjl0TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFrTWZPV2lERQogICAgICAgICAgICAgICAgICAgICAgICBrK05uRnppK0dEMW1xMCtneUxmRXkwbHdYSHFIakFXbVVEMGZCS0k1TUtMamhTMGduRURYRHZINFBDdW1majE2RmdMdDRQcVlRVFJjCiAgICAgICAgICAgICAgICAgICAgICAgIHkvYW9DSE9TY3U4UnBDUEtyYXFwWGZDWU9qeDZIWTRvYmZKMTA3eTl2L0VhUnVRYlBrc1Y1VHo5WTRKY2tLRUN3UCtManh5amhJYlUKICAgICAgICAgICAgICAgICAgICAgICAgL3FVMndkVnhydUN2RlcxMnZDVFpqTmhGR2h4Vm1LdkNhU0luQnNXRE9peTJyWjRHL05FakhLYU1FbTI0MFg1QW9HbXVaTFB5YTZxVAogICAgICAgICAgICAgICAgICAgICAgICBmbXQ2b2kwQXZOa0FpZWd0YTF1RzhyMk92OFJyV3lwNHBaQ0kralBLa01rZXErZGtxZVhqUU5TUFNDZHBxeHlKbmg0a0RuNmEweFFPCiAgICAgICAgICAgICAgICAgICAgICAgIHNsSG1HNnhsbUI0c0ZvalBWUDFLS2ZIc3BxTWVvUUlEQVFBQk1BMEdDU3FHU0liM0RRRUJCUVVBQTRJQkFRQmx2UlVoTFlhL0V1UTYKICAgICAgICAgICAgICAgICAgICAgICAgOW1iZHd5V1RvVXpOckpxaG9YbjdzaWJickdzYzFPZGRKVWNnQkVBNVVzcUh6RzJiRms5Unh5N3p4Wklsb3VGQy9HMHJLOFZ3Y3pTQwogICAgICAgICAgICAgICAgICAgICAgICA0RGNVVndtVForRkg2djhMM1JpS2ZSRUNTM05GQ2pERzhlNzFjbHhFY09oWnQybTQ3OFdhdkNXYWY0V3FEZnErOUF3clBpOUptUHV3CiAgICAgICAgICAgICAgICAgICAgICAgIG9GNkE5a2p3Y1BuWHM1ZWk0UC9CQWdGUVFPOEYxS0pVNUk5SWUwSjZ2L05Icy90QkExSXN2TzFPNDBHbmxzSmt4Y1QvbGhRZnVYc0MKICAgICAgICAgICAgICAgICAgICAgICAgWFRLMHVKa1liQzJGSzh1QzNZVlBGNmpqOHFtOUN1ZW9vSUYybE1uenpGN0QrS2JRM1MvR2F0Mm0xaWRlNnowTXIrOE1wL04zdHkyRwogICAgICAgICAgICAgICAgICAgICAgICB2UlZTb09oUnR1RW85ZTF0Z1VyZzdFelgKICAgICAgICAgICAgICAgICAgICA8L2RzOlg1MDlDZXJ0aWZpY2F0ZT4KICAgICAgICAgICAgICAgIDwvZHM6WDUwOURhdGE+CiAgICAgICAgICAgIDwvZHM6S2V5SW5mbz4KICAgICAgICA8L2RzOlNpZ25hdHVyZT4KICAgICAgICA8c2FtbDI6U3ViamVjdCB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+CiAgICAgICAgICAgIDxzYW1sMjpOYW1lSUQgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCI+ZmFrZW5hbWVAYWNtZS5leGFtcGxlLmNvbQogICAgICAgICAgICA8L3NhbWwyOk5hbWVJRD4KICAgICAgICAgICAgPHNhbWwyOlN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj4KICAgICAgICAgICAgICAgIDxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBOb3RPbk9yQWZ0ZXI9IjIwMTktMDEtMDhUMTM6MDc6MDMuMDIwWiIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBSZWNpcGllbnQ9Imh0dHBzOi8vc2lnbmluLmF3cy5hbWF6b24uY29tL3NhbWwiLz4KICAgICAgICAgICAgPC9zYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uPgogICAgICAgIDwvc2FtbDI6U3ViamVjdD4KICAgICAgICA8c2FtbDI6Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMTktMDEtMDhUMTI6NTc6MDMuMDIwWiIgTm90T25PckFmdGVyPSIyMDE5LTAxLTA4VDEzOjA3OjAzLjAyMFoiCiAgICAgICAgICAgICAgICAgICAgICAgICAgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPgogICAgICAgICAgICA8c2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj4KICAgICAgICAgICAgICAgIDxzYW1sMjpBdWRpZW5jZT51cm46YW1hem9uOndlYnNlcnZpY2VzPC9zYW1sMjpBdWRpZW5jZT4KICAgICAgICAgICAgPC9zYW1sMjpBdWRpZW5jZVJlc3RyaWN0aW9uPgogICAgICAgIDwvc2FtbDI6Q29uZGl0aW9ucz4KICAgICAgICA8c2FtbDI6QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDE5LTAxLTA4VDEzOjAyOjAzLjAyMFoiIFNlc3Npb25JbmRleD0iaWQxNTQ2OTUyNTIzMDIwLjEwOTgyODg2NDciCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj4KICAgICAgICAgICAgPHNhbWwyOkF1dGhuQ29udGV4dD4KICAgICAgICAgICAgICAgIDxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydAogICAgICAgICAgICAgICAgPC9zYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj4KICAgICAgICAgICAgPC9zYW1sMjpBdXRobkNvbnRleHQ+CiAgICAgICAgPC9zYW1sMjpBdXRoblN0YXRlbWVudD4KICAgICAgICA8c2FtbDI6QXR0cmlidXRlU3RhdGVtZW50IHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj4KICAgICAgICAgICAgPHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJodHRwczovL2F3cy5hbWF6b24uY29tL1NBTUwvQXR0cmlidXRlcy9Sb2xlIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj4KICAgICAgICAgICAgICAgIDxzYW1sMjpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOnN0cmluZyI+CiAgICAgICAgICAgICAgICAgICAgYXJuOmF3czppYW06OjY2MDUwMjc1NDY2MjpzYW1sLXByb3ZpZGVyL2FjbWUub2t0YS5jb20sYXJuOmF3czppYW06OjEyMzQ1Njc4OTAxMjpyb2xlL0VDMkFkbWlucwogICAgICAgICAgICAgICAgPC9zYW1sMjpBdHRyaWJ1dGVWYWx1ZT4KICAgICAgICAgICAgPC9zYW1sMjpBdHRyaWJ1dGU+CiAgICAgICAgICAgIDxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iaHR0cHM6Ly9hd3MuYW1hem9uLmNvbS9TQU1ML0F0dHJpYnV0ZXMvUm9sZVNlc3Npb25OYW1lIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6YmFzaWMiPgogICAgICAgICAgICAgICAgPHNhbWwyOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4c2k6dHlwZT0ieHM6c3RyaW5nIj4KICAgICAgICAgICAgICAgICAgICBmYWtlbmFtZUBhY21lLmV4YW1wbGUuY29tCiAgICAgICAgICAgICAgICA8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPgogICAgICAgICAgICA8L3NhbWwyOkF0dHJpYnV0ZT4KICAgICAgICAgICAgPHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJodHRwczovL2F3cy5hbWF6b24uY29tL1NBTUwvQXR0cmlidXRlcy9TZXNzaW9uRHVyYXRpb24iCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDpiYXNpYyI+CiAgICAgICAgICAgICAgICA8c2FtbDI6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czpzdHJpbmciPjE0NDAwCiAgICAgICAgICAgICAgICA8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPgogICAgICAgICAgICA8L3NhbWwyOkF0dHJpYnV0ZT4KICAgICAgICA8L3NhbWwyOkF0dHJpYnV0ZVN0YXRlbWVudD4KICAgIDwvc2FtbDI6QXNzZXJ0aW9uPgo8L3NhbWwycDpSZXNwb25zZT4=";

    @Test
    void main() throws Exception {
        OktaAwsCliEnvironment environment = new OktaAwsCliEnvironment(false, null, null, null, null, null, "https://acmecorp.oktapreview.com/home/amazon_aws/0oa5zrwfs815KJmVF0h7/137", null, 0, null, null, false, null);
        RoleHelper roleHelper = new RoleHelper(environment);
        List<AccountOption> availableRoles = roleHelper.getAvailableRoles(EXAMPLE_SAML_RESPONSE);
        assertEquals(singletonList(
                new AccountOption("Account:  (123456789012)", singletonList(
                        new RoleOption("EC2Admins", "arn:aws:iam::123456789012:role/EC2Admins")
                ))
        ), availableRoles);
    }
}