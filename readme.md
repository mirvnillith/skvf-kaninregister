# SKVF Kaninregister

Hanterar registrering av och sökning på kaniner.

## Applikation

Kaninregistret är en Spring Boot applikation som använder Google Spread Sheet som lagring och exponerar ett REST (Open)API.

Byggs mha Maven och producerar en tillståndslös Docker image för fristående körning.

## Konfiguration

Kaninregistret kräver följande konfigureringar:

**OBS:** Ha aldrig med dessa värden i källkod eller andra filer du lagrar i GitHub!!!

##### APPROVAL_URL

URL till PDF att signera (lämnas tom för ingen signering).

##### ADDO_URL

URL till Visma Addo signeringstjänst (lämnas tom för offline-signering eller om APPROVAL_URL är tom).

##### ADDO_USERNAME

Visma Addo användarnamn (lämnas tom för offline-signering eller om APPROVAL_URL är tom).

##### ADDO_PASSWORD

Visma Addo lösenord (lämnas tom för offline-signering eller om APPROVAL_URL är tom).

##### GOOGLE_CREDENTIALS

Autentiseringsuppgifter för Google Drive, där Kaninregistret kommer att ha en egen yta.

Skapa ett Service Account i ett projekt på Google Cloud. Generera sedan en API-nyckel och ange den resulterande filens innehåll som värde. För att förenkla hantering i konfigurationsverktyg kan man mha `se.skvf.kaninregister.drive.GoogleDrive.main` ersätta " med § och \ med ¤ samt ta bort nyrader.

##### GOOGLE_FOLDER

ID för delad Google Drive mapp så kalkylbladet blir synligt även för dig.

Skapa en mapp i din Google Drive och dela den med Service Account ovan via dess mailadress. Katalogens ID finns sist i URLn när du står i den.

## Lokal uppstart

Kaningregistret går även att köra utanför Docker, men kräver då en `application.yaml` i arbetskatalogen. Kopiera från `src/main/docker` och ersätt miljövariablerna med värden.

Dessutom finns tre flaggor, `skvf.dev.test`, `skvf.dev.performance` och `skvf.dev.addo`, för provkörning och/eller prestandakörning automatiskt vid uppstart.
