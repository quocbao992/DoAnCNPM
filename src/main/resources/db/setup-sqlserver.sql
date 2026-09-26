USE master;
GO

IF DB_ID(N'brewlite') IS NULL
BEGIN
    CREATE DATABASE brewlite COLLATE Vietnamese_CI_AS;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'brewlite_login')
BEGIN
    CREATE LOGIN brewlite_login
        WITH PASSWORD = 'BrewLite@2026',
        CHECK_POLICY = ON,
        CHECK_EXPIRATION = OFF;
END;
GO

USE brewlite;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'brewlite_user')
BEGIN
    CREATE USER brewlite_user FOR LOGIN brewlite_login;
END;
GO

IF IS_ROLEMEMBER(N'db_owner', N'brewlite_user') <> 1
BEGIN
    ALTER ROLE db_owner ADD MEMBER brewlite_user;
END;
GO
