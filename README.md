# StayPark

StayPark ([staypark.com](https://www.staypark.com)) is a website that helps travelers find affordable airport parking at hotels near the airport. Hotels ("affiliates") list spare parking spaces, and travelers book and pay for parking online, either parking only or bundled with a hotel room ("Room & Park").

## Features

**For travelers**
- Search airport parking by airport, city, or state, and browse all supported airports
- Compare nearby hotel lots by price, distance from the airport, or recommendation
- Book parking-only or hotel room + parking (Room & Park) reservations
- Online checkout with credit-card payments through Authorize.net, plus coupons and access fees
- Change, extend, or cancel a reservation
- Customer accounts: sign up, log in, profile, reservation history, change or forgot password
- Email confirmations and receipts

**For hotel affiliates**
- Affiliate registration and login
- View and manage reservations at their property
- Room and parking inventory, blocked or special-rate dates, minimum-stay rules
- Financial and reservation reports, including a monthly CSV export

**Site**
- Home page, FAQ, About, Contact ("Contact Us"), Terms, Privacy, Sitemap
- Google reCAPTCHA on public forms
- SEO-friendly airport landing pages, PWA manifest and service worker
- Health checks at `/heartbeat` and `/deep-heartbeat`

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java (compiled to Java 8 bytecode; runs on Java 11) |
| Framework | Spring Boot 2.3.4 (Web, Security, AOP, Actuator) |
| Views | Thymeleaf + Thymeleaf Layout Dialect, jQuery 3.7, Bootstrap |
| Database | MySQL (JDBC via `mysql-connector-java`) |
| Payments | Authorize.net Java SDK |
| Email | JavaMail over Amazon SES SMTP |
| Logging | Log4j2 (2.17.1+) with LMAX Disruptor async logging |
| Build | Maven |

## Project structure

```
src/main/java/staypark/parking/
├── Application.java        # Spring Boot entry point
├── aspect/                 # AOP logging
├── beans/                  # DTOs/models (booking, hotel, payment, reservation, security, user...)
├── comparator/             # Hotel sort orders (price, distance, recommended)
├── config/                 # Spring Security config and custom auth provider
├── controller/
│   ├── nonsecure/          # Public pages: home, search, checkout, login, reports
│   └── secure/             # Logged-in customer and affiliate pages
├── filter/                 # Response header filter
├── handler/                # Business logic and data access (accounts, checkout, payments, hotels...)
├── health/                 # Heartbeat endpoints
├── restcontroller/         # JSON endpoints (location lookups, account checks)
└── util/                   # DB, email, date, file and security helpers
src/main/resources/
├── application.properties
├── log4j2.xml
├── static/                 # CSS, JS, images, manifest, service worker
└── templates/              # Thymeleaf HTML templates
```

## Getting started

### Prerequisites
- JDK 11
- Maven 3.6+
- A MySQL database with the StayPark schema (`dbstaypark`)

### Configuration

No credentials are committed to this repository. Before running, set the following values (they are currently constants in the classes listed, see the [roadmap](ROADMAP.md) for moving them to environment variables):

| Setting | Location |
|---|---|
| Database host, user, password | `util/DBUtil.java` |
| SMTP host, port, user, password, from/support addresses | `util/EmailUtil.java` |
| Authorize.net API login ID and transaction key | `handler/PaymentHandler.java` |
| reCAPTCHA secret key | `util/SecurityUtil.java` |
| SSL keystore (optional, for HTTPS) | `src/main/resources/keystore.p12` + `application.properties` |

The keystore is git-ignored. Generate your own for local HTTPS:

```bash
keytool -genkeypair -alias staypark -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore.p12 -validity 365
```

### Build and run

```bash
mvn clean package
java -jar target/customer-web-1.1.0-RELEASE.jar
```

Or, during development:

```bash
mvn spring-boot:run
```

The site runs at http://localhost:8080 by default.

## Roadmap

See [ROADMAP.md](ROADMAP.md) for planned work, including an admin portal, a React front end, containerization, feature enhancements, and bug fixes.
