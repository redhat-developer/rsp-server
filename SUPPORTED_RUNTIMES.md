# Supported Runtimes

This document lists the server runtimes that RSP can discover, create, start, stop, and publish to, along with the Java versions each runtime supports.

Downloadable runtimes are fetched dynamically from [jboss-stacks](https://github.com/jboss-developer/jboss-stacks). The tables below reflect the server types registered in the RSP codebase.

## WildFly

| Server Type | Versions | Min Java | Max Java |
|-------------|----------|----------|----------|
| WildFly 8.x | 8.0 – 8.2 | 7 | 8 |
| WildFly 9.x | 9.0 | 7 | 8 |
| WildFly 10.x | 10.0 – 10.1 | 8 | 8 |
| WildFly 11.x | 11.0 | 8 | 9 |
| WildFly 12.x | 12.0 | 8 | 10 |
| WildFly 13.x | 13.0 | 8 | 10 |
| WildFly 14.x | 14.0 | 8 | 10 |
| WildFly 15.x | 15.0 | 8 | 11 |
| WildFly 16.x | 16.0 | 8 | 12 |
| WildFly 17.x | 17.0 | 8 | 13 |
| WildFly 18.x | 18.0 | 8 | 13 |
| WildFly 19.x | 19.0 | 8 | 13 |
| WildFly 20.x | 20.0 | 8 | 15 |
| WildFly 21.x | 21.0 | 8 | 15 |
| WildFly 22.x | 22.0 | 8 | 15 |
| WildFly 23.x | 23.0 | 8 | 15 |
| WildFly 24+ | 24.0 – 26.x | 8 | 21 |
| WildFly 27+ | 27.0 – 34.x | 11 | 21 |
| WildFly 35+ | 35.0 – 37.x | 17 | 21 |
| WildFly 38+ | 38.0+ | 17 | 25 |

## JBoss EAP

| Server Type | Min Java | Max Java |
|-------------|----------|----------|
| JBoss EAP 4.3 | 4 | 5 |
| JBoss EAP 5.0 | 6 | 8 |
| JBoss EAP 6.0 | 6 | 8 |
| JBoss EAP 6.1 | 6 | 8 |
| JBoss EAP 7.0 | 8 | 8 |
| JBoss EAP 7.1 | 8 | 8 |
| JBoss EAP 7.2 | 8 | 11 |
| JBoss EAP 7.3 | 8 | 15 |
| JBoss EAP 7.4 | 8 | 21 |
| JBoss EAP 8.0 | 11 | 21 |

EAP downloads require Red Hat credentials.

## JBoss AS (Legacy)

| Server Type | Min Java | Max Java |
|-------------|----------|----------|
| JBoss AS 3.2 | 3 | 5 |
| JBoss AS 4.0 | 4 | 5 |
| JBoss AS 4.2 | 5 | 6 |
| JBoss AS 5.0 | 5 | 6 |
| JBoss AS 5.1 | 5 | 6 |
| JBoss AS 6.0 | 6 | 8 |
| JBoss AS 7.0 | 6 | 7 |
| JBoss AS 7.1 | 6 | 7 |

These are legacy server types retained for backward compatibility.

## Container Development

| Server Type | Description |
|-------------|-------------|
| Minishift 1.12+ | OpenShift local development (Minishift) |
| CDK 3.x | Red Hat Container Development Kit |
| CRC 1.x | Red Hat CodeReady Containers (OpenShift Local) |

## EAP to WildFly Mapping

JBoss EAP is the commercially supported downstream of WildFly. The approximate mapping is:

| EAP Version | WildFly Upstream |
|-------------|-----------------|
| EAP 7.0 | WildFly 10 |
| EAP 7.1 | WildFly 11 |
| EAP 7.2 | WildFly 14 |
| EAP 7.3 | WildFly 18 |
| EAP 7.4 | WildFly 23 |
| EAP 8.0 | WildFly 27 |

See [Red Hat's official mapping](https://access.redhat.com/solutions/21906) for details.
