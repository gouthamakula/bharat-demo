db = db.getSiblingDB('udl-graphql');
db.createUser(
    {
        user: "udl_user",
        pwd: "udl_password",
        roles: [
            { role: "readWrite", db: "udl-graphql" },
            { role: "dbAdmin", db: "udl-graphql" }
        ]
    }
);