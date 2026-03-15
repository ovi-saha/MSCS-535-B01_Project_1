# MSCS-535-B01_Project_1
This Repo is Project 1 for Secure Software Development (MSCS-535-B01)
# Secure-MFA-System 

A Java-based web application demonstrating enterprise-grade security protocols. This project specifically addresses **Defense-in-Depth** strategies against Phishing and SQL Injection as part of the Secure Software Development (MSCS-535-B01) project.

##  Core Features

### 1. Multi-Factor Authentication (MFA/2FA)
* **TOTP Algorithm:** Implements the Time-based One-Time Password (RFC 6238) algorithm.
* **Device Sync:** Generates scannable QR codes compatible with Google Authenticator, Authy, or iOS Passwords.
* **Session Hardening:** Prevents "MFA Bypassing" by requiring a session-specific verification stamp before granting access to protected routes.

### 2. Database & Application Security
* **SQL Injection Prevention:** Utilizes **Spring Data JPA** and **Hibernate**. All database interactions use Parameterized Queries (Prepared Statements), ensuring user input is treated strictly as data, never as executable code.
* **Persistent Storage:** Uses an **H2 In-Memory Database** for secure, temporary data handling during runtime.

### 3. Transport Layer Security (TLS)
* **HTTPS Only:** Configured to run over SSL/TLS using a PKCS12 keystore.
* **Encryption:** Ensures all data "in-flight" (passwords, MFA codes, session IDs) is encrypted, mitigating packet-sniffing and Man-in-the-Middle (MITM) attacks.

---

##  Tech Stack
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security
* **Database:** H2 (JPA/Hibernate)
* **MFA Engine:** `dev.samstevens.totp`
* **QR Generator:** Google ZXing
* **Environment:** MacBook M3 Silicon

---

##  Installation & Setup
- Clone this project to your system then open it in intelliJ IDEA or eclips. Then, 
### 1. Generate SSL Certificate
Run the following command in your terminal to create the required `.p12` keystore:
```bash
keytool -genkeypair -alias secureapp -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore.p12 -validity 3650
```

### 2. Configure Environment

Update src/main/resources/application.properties:
Properties

```
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your_keystore_password
```

### 3. Build and Run
Bash

```
mvn clean install
mvn spring-boot:run
```

## Testing Workflow (Postman)

To verify the security layers, follow this sequence:

**Register:** POST https://localhost:8443/api/register

    - Register a new user with a JSON payload.
    

**Login:** POST https://localhost:8443/api/login

    - Authenticate with password. Returns 200, but session is not yet authorized for the dashboard.

**Attempt Dashboard:** GET https://localhost:8443/api/dashboard

     - Result: 401 Unauthorized (MFA Required).

**Setup MFA:** GET https://localhost:8443/api/mfa/setup

    - Result: Scannable QR Code and Secret Key generation.

**Verify:** POST https://localhost:8443/api/mfa/verify?email=user@example.com&code=123456

    - Provide the 6-digit code from your mobile device.

**Success:** GET https://localhost:8443/api/dashboard

    - Result: 200 OK (Secure data accessed).

## Security Analysis (Assignment Compliance)
| Attack Vector     | Defense Mechanism          | Implementation Detail |
|-------------------|---------------------------|----------------------|
| Phishing          | Two-Factor Authentication | Even if credentials are stolen via social engineering, the attacker lacks the physical device required to generate the TOTP code. |
| SQL Injection     | Parameterized Queries     | JPA Repository abstracts SQL execution, preventing malicious input from altering the query structure. |
| Packet Sniffing   | HTTPS/SSL                 | All credentials and session cookies are encrypted via TLS 1.3 before being transmitted over the network. |
