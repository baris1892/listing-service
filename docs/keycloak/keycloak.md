# Keycloak Setup

### Access Keycloak

- **Keycloak URL**: [http://localhost:8081](http://localhost:8081)
- **Admin Console**: [http://localhost:8081/admin/](http://localhost:8081/admin)
- **Login URL**: http://localhost:8081/realms/portfolio/account

Login with the credentials defined in `docker-compose.yml` via `KEYCLOAK_ADMIN` and `KEYCLOAK_ADMIN_PASSWORD`:

```
Username: admin
Password: admin
```

---

### Importing a Realm

You can import the provided `realm-export.json` via the **Admin Console**.  
This will create a realm named **`portfolio`**.

---

### Create Test User

Execute the following bash script:  
`bash docs/keycloak/create-user.sh user1 user1@example.com`  
The password will be automatically set to `password` for demo reasons.

---

### Retrieve Access Token for User

Execute the following bash script:  
`bash docs/keycloak/get-token.sh user1`

Additionally, you can decode the JWT if you want:  
`bash docs/keycloak/get-token.sh user1 decode`

---

### Testing with Keycloak Demo App

Keycloak provides a hosted test application: [https://www.keycloak.org/app](https://www.keycloak.org/app).  
You can use it to verify that your local Keycloak server is working correctly.

Use the following settings:

- **Keycloak URL**: `http://localhost:8081`
- **Realm**: `portfolio`
- **Client**: `public-client`

After entering these values you’ll be redirected to the Keycloak login page.  
Log in, and you should be redirected back to the test app:  
[https://www.keycloak.org/app/#url=http://localhost:8081&realm=portfolio&client=public-client](https://www.keycloak.org/app/#url=http://localhost:8081&realm=portfolio&client=public-client)

---
✅ With this setup you can quickly verify both the **admin access** and a **working client login flow**.
