# LIFE - premier prototype Android

Ce dossier contient le premier squelette Android natif de LIFE, construit avec Kotlin et Jetpack Compose.

## Ce qui fonctionne dans ce prototype
- Interface d'accueil colorée et claire
- Liste de missions de démonstration
- Validation manuelle des missions
- Attribution de XP et de pièces en mémoire
- Écrans Collection, Récompenses et Profil

## Ce qui n'est pas encore implémenté
- Sauvegarde persistante des données
- Création/édition des missions
- Capture et vérification des photos
- Minuteurs de mission
- Animal et évolutions définitifs
- Blocage des applications et pauses récompenses

Le blocage n'est volontairement pas simulé : il devra être développé séparément avec les API Android appropriées, puis testé sur un appareil réel.

## Compilation
Prérequis : Android Studio récent, JDK 17, Android SDK 35 et Gradle 8.9 ou version compatible avec le plugin Android Gradle 8.7.3.

Ouvrir le dossier dans Android Studio, laisser Gradle synchroniser le projet, puis lancer l'application sur un appareil Android ou créer un APK debug via Build > Build Bundle(s) / APK(s) > Build APK(s).

Application ID : `com.lifequest.life`
Version : `0.1.0`
