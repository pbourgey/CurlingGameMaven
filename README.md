# CurlingGameMaven

## Tuto pour créer un projet Maven avec toutes les dépendances nécessaires
### Tuto pour Windows10/11 64x

Pour installer toutes les dépendances :

- Installer le plug-in "e(fx)clipse" depuis le marketplace de eclipse.
- Créer un projet JavaFx.
- Le convertir en projet Maven en faisant    -> clic droit -> configure -> Convertir en projet Maven
- modifier le pom.xml de sorte à ce qu'il ressemble à celui actuel (ajout des dépendances et des configurations de build)
- ajouter "requires opencv;" dans le fichier module-info.java
- ajouter "requires java.desktop;" dans le fichier module-info.java



Une fois toutes les dépendances renseignées :

- Télécharger la version 4.5.1 windows64x (Source) de openCV en suivant ce lien : https://opencv.org/releases/
- Placer le dossier dans le dossier racine du projet.
- Faire un clic droit sur le fichier .java qu'on veut exécuter dans l'IDE et sélectionner  ->run as->run configurations...
- Dans "arguments" -> "VM argument", écrire : "-Djava.library.path=.\opencv\build\java\x64" pour indiquer le chemin vers le fichier .dll requis
- Changer le chemin en conséquence si vous n'avez pas placé le dossier au même endroit
