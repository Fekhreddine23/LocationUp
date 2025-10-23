#!/bin/bash

echo "🚀 Début de la restructuration..."

# Créer la nouvelle structure
mkdir -p src/app/core/{services,guards,interceptors,models}
mkdir -p src/app/shared/{components,directives,pipes,utils}
mkdir -p src/app/pages

echo "📁 Structure créée"

# Déplacer les dossiers
if [ -d "src/app/components/login" ]; then
    mv src/app/components/login src/app/pages/
    echo "✅ Login déplacé"
else
    echo "⚠️  Login non trouvé"
fi

if [ -d "src/app/components/register" ]; then
    mv src/app/components/register src/app/pages/
    echo "✅ Register déplacé"
else
    echo "⚠️  Register non trouvé"
fi

# Renommer les fichiers components
if [ -d "src/app/pages/login" ]; then
    cd src/app/pages/login
    mv login.ts login.component.ts 2>/dev/null || echo "ℹ️  login.ts déjà renommé ou absent"
    mv login.html login.component.html 2>/dev/null || echo "ℹ️  login.html déjà renommé ou absent"
    mv login.scss login.component.scss 2>/dev/null || echo "ℹ️  login.scss déjà renommé ou absent"
    cd -
    echo "✅ Fichiers login renommés"
fi

if [ -d "src/app/pages/register" ]; then
    cd src/app/pages/register
    mv register.ts register.component.ts 2>/dev/null || echo "ℹ️  register.ts déjà renommé ou absent"
    mv register.html register.component.html 2>/dev/null || echo "ℹ️  register.html déjà renommé ou absent"
    mv register.scss register.component.scss 2>/dev/null || echo "ℹ️  register.scss déjà renommé ou absent"
    cd -
    echo "✅ Fichiers register renommés"
fi

echo "🎉 Restructuration terminée !"
echo ""
echo "📝 Prochaines étapes :"
echo "  1. Mettre à jour les imports dans vos fichiers"
echo "  2. Déplacer auth.service.ts dans src/app/core/services/"
echo "  3. Vérifier les routes dans app.routing.ts"
echo "  4. Lancer 'ng serve' pour vérifier les erreurs"