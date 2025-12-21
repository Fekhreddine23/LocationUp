# Design Tokens & Theming Guide

Ce fichier regroupe les variables CSS définies dans `frontend/src/styles.scss` et explique comment les utiliser pour garder une interface cohérente en mode clair et sombre.

## Tokens principaux

| Variable | Usage | Exemple | Mode |
|---|---|---|---|
|`--text-primary`|Couleur principale du texte des cartes et des titres|`color: var(--text-primary);`|adaptée (change selon `.dark-theme`)|
|`--text-secondary`|Texte secondaire, légendes, aides visuelles|`color: var(--text-secondary);`|idem|
|`--card-bg`|Fond des surfaces (cartes, sections profil)|`background: var(--card-bg);`|blanc en clair, foncé en sombre|
|`--card-border`|Couleur des bordures de cartes|`border: 1px solid var(--card-border);`||
|`--card-shadow`|Ombre portée des surfaces contextuelles|`box-shadow: var(--card-shadow);`||
|`--input-bg`|Fond des champs de saisie|`background: var(--input-bg);`|assure contraste saisie|
|`--primary-color` / `--primary-hover`|Actions principales, liens, statuts|`color: var(--primary-color);`||

## Bonnes pratiques

1. **Réutiliser les tokens dans tous les composants** : par exemple, la section `.preferences-section` (`profile.component.scss`) utilise `var(--text-primary)` pour les titres et `var(--card-bg)` pour le fond. Quand tu ajoutes une nouvelle carte, pense à utiliser `var(--card-border)` pour la bordure plutôt qu’un `#ccc`.

2. **Éviter les overrides ciblés** : au lieu d’ajouter `.light-theme` ou `prefers-color-scheme` à chaque bloc, définis les couleurs via les tokens. Seules les variantes globales (.dark-theme) doivent ajuster la valeur des tokens.

3. **Documenter les helpers d’accessibilité** : les sections profil utilisent maintenant des `sr-only` et `aria-describedby`. Garde un modèle de structure (titre + description + champ) pour garantir une lecture correcte par les lecteurs d’écran.

4. **Storybook ou tests visuels** : lorsque tu crées des stories (p.ex. “Profil – préférences”), utilise les tokens pour construire les états clair et sombre. Tu peux importer `styles.scss` ou mocker les classes `.dark-theme` pour montrer la version nuit.

## Étapes pour réutiliser

1. Importer les variables globales (`@import '~src/styles.scss'` si besoin).
2. Appliquer `background: var(--card-bg); color: var(--text-primary); border: 1px solid var(--card-border);`.
3. Pour un élément interactif, ajouter `transition: box-shadow 0.3s` et `box-shadow: var(--card-shadow);` en hover.
4. Pour les sections toggles, utiliser `accent-color: var(--text-primary);` pour que le contrôle reste visible en clair et sombre.

## Storybook / tests visuels (suggestion)

- Créer une story “Profile / Sections principales” où tu appliques `.dark-theme` sur `document.documentElement` avec `decorators`.
- Monter les sections `.preferences-section`, `.security-section`, `.history-section` et `.identity-section` côte à côte.
- Vérifier les contrastes et capturer un screenshot sur chaque mode pour le comparer en CI (outil comme Chromatic ou Percy).

Cette documentation pourra ensuite être dupliquée dans un README général `frontend/docs/README.md` si tu souhaites étendre la palette à d’autres pages.
