# HOW TO RUN THIS FUCKING PROJECT 🖕💀

Listen up, you absolute disasters. I'm only going to say this once. If you message me asking "why isn't it working" and you haven't followed this exact sequence, I will come to your house and delete your system32.

This assumes you have Java, Docker, and Node installed. If you don't, go back to kindergarten.

### 1. START THE DAMN FRONTEND
Don't ask questions. Just start the UI so you have something pretty to look at while your RAM cries.

**Before you run `nx start frontend`:**

You have two choices, peasant:

*   **Choice 1: Global NX (Recommended for frequent use)**
    If you plan on living and breathing NX, install it globally. This way, `nx` commands just work.
    ```bash
    npm install -g nx
    ```

*   **Choice 2: Use NPX (Lazy but works)**
    If you're a commitment-phobe, `npx` will fetch and run `nx` on the fly. It's slower, but less messy.
    ```bash
    npx nx start frontend
    ```
    (Note: If you go this route, replace `nx start frontend` below with `npx nx start frontend`)

**Now, actually start the frontend:**

```bash
nx start frontend
```

### 2. THE GATEWAY (THE BOUNCER)
Open a new terminal. Do it. Run this. This checks the IDs. If this is down, nobody gets in.

```bash
./gradlew :apps:infrastructure:graphql-gateway:bootRun
```

### 3. SERVICE DISCOVERY (WHERE THE FUCK IS EVERYONE?)
New terminal. This tells the services where the other services are hiding.

```bash
./gradlew :apps:infrastructure:service-discovery:bootRun
```

### 4. POSTGRES (THE DATA DUMP)
We need a database. Obviously.

```bash
docker compose up postgres
```

### 5. KAFKA (THE SCREAMING VOID)
Start the message broker. It eats RAM for breakfast.

```bash
docker compose up kafka
```

### 6. USER SERVICE (THE PEOPLE FARM)
Now we actually need the logic for the meatbags (users).

```bash
./gradlew :apps:services:user-service:bootRun
```

### 7. KEYCLOAK (THE OVERLORD)
This handles the logins. We build it to make sure the realm import shit works.

```bash
docker compose up keycloak --build
```

### 8. AUTH SERVICE (THE POLICE)
Finally, start the auth service.

```bash
./gradlew :apps:services:auth-service:bootRun
```

---

**If it doesn't work:**
1. Check your ports.
2. Check your Docker.
3. Cry.
4. Fix it yourself.

**Good luck.**
