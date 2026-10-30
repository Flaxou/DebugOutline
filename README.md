# Debug Outline (Fabric 1.21.4)

Mod client : contour orange autour des coffres (y compris piégés) dans un cube de 64x64x64
centré sur le joueur, visible à travers les murs. Touche par défaut : H (modifiable dans Options > Contrôles).

## Compiler

1. Installer JDK 21.
2. Générer le wrapper Gradle si besoin : `gradle wrapper --gradle-version 8.10` (ou importer dans IntelliJ).
3. `./gradlew build` (ou `gradlew.bat build` sous Windows).
4. Le jar se trouve dans `build/libs/debugoutline-1.0.0.jar`. Le glisser dans `mods/` avec Fabric API 0.119.x pour 1.21.4.

## Réglages

- `RADIUS` dans `DebugOutlineClient.java` (32 par défaut, soit un cube de 64 blocs de côté).
- `SCAN_INTERVAL_TICKS` pour la fréquence de scan.
