describe('Complete Form Debug', () => {
  it('should inspect all form elements', () => {
    cy.visit('/login')
    
    // Capture initiale
    cy.screenshot('debug-initial')
    
    // Affiche TOUS les éléments de la page
    cy.log('=== PAGE CONTENT ANALYSIS ===')
    cy.get('body').then(($body) => {
      const text = $body.text()
      cy.log('All visible text (first 500 chars):', text.substring(0, 500))
    })
    
    // Analyse tous les éléments de formulaire
    cy.log('=== FORM ELEMENTS ===')
    
    // Tous les inputs
    cy.get('input').each(($input, index) => {
      const attrs = {
        type: $input.attr('type'),
        name: $input.attr('name'),
        placeholder: $input.attr('placeholder'),
        id: $input.attr('id'),
        class: $input.attr('class'),
        'data-*': Object.keys($input[0].dataset).join(', ')
      }
      cy.log(`Input ${index}:`, JSON.stringify(attrs))
    })
    
    // Tous les boutons
    cy.get('button, input[type="submit"], [role="button"]').each(($btn, index) => {
      const attrs = {
        text: $btn.text().trim(),
        type: $btn.attr('type'),
        class: $btn.attr('class'),
        id: $btn.attr('id')
      }
      cy.log(`Button ${index}:`, JSON.stringify(attrs))
    })
    
    // Tous les labels
    cy.get('label').each(($label, index) => {
      const attrs = {
        text: $label.text().trim(),
        for: $label.attr('for'),
        class: $label.attr('class')
      }
      cy.log(`Label ${index}:`, JSON.stringify(attrs))
    })
    
    // Tous les placeholders
    cy.get('[placeholder]').each(($el, index) => {
      cy.log(`Placeholder ${index}:`, $el.attr('placeholder'))
    })
    
    // Capture après analyse
    cy.screenshot('debug-after-analysis')
    
    // Test de remplissage avec différentes méthodes
    cy.log('=== TESTING DIFFERENT SELECTORS ===')
    
    // Méthode 1: Par index
    cy.get('input').eq(0).type('testuser', { force: true })
    cy.get('input').eq(1).type('password123', { force: true })
    
    // Capture formulaire rempli
    cy.screenshot('debug-form-filled')
    
    // Méthode 2: Trouver le bouton par texte approximatif
    cy.get('button, input[type="submit"]').then(($buttons) => {
      const connectButtons = $buttons.filter((index, btn) => {
        const text = btn.textContent?.toLowerCase() || ''
        return text.includes('connect') || text.includes('connexion') || text.includes('login') || text === ''
      })
      
      if (connectButtons.length > 0) {
        cy.log(`Found ${connectButtons.length} possible submit buttons`)
        cy.wrap(connectButtons.first()).click({ force: true })
      } else {
        cy.log('No submit button found, trying first button')
        cy.get('button').first().click({ force: true })
      }
    })
    
    // Capture résultat
    cy.wait(3000)
    cy.screenshot('debug-after-submit')
    
    // Analyse finale
    cy.url().then(url => cy.log('Final URL:', url))
    cy.document().then(doc => cy.log('Page title:', doc.title))
  })
})
