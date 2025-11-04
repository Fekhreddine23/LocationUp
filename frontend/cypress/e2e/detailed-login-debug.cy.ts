describe('Detailed Login Debug', () => {
  it('should capture detailed login information', () => {
    cy.visit('/login')
    
    // Capture 1: Page login initiale
    cy.screenshot('1-initial-login-page')
    
    // Log tous les éléments de formulaire
    cy.get('form').then($form => {
      cy.log('Form found:', $form.length > 0)
      if ($form.length > 0) {
        cy.log('Form action:', $form.attr('action'))
        cy.log('Form method:', $form.attr('method'))
      }
    })
    
    cy.get('input').each(($input, index) => {
      const type = $input.attr('type')
      const name = $input.attr('name')
      const placeholder = $input.attr('placeholder')
      const id = $input.attr('id')
      cy.log(`Input ${index}: type=${type}, name=${name}, placeholder=${placeholder}, id=${id}`)
    })
    
    // Remplit le formulaire
    cy.get('input[type="text"], input[type="email"], input[name="username"], input:first')
      .first()
      .type('testuser', { force: true })
    
    cy.get('input[type="password"], input[name="password"], input:eq(1)')
      .first()  
      .type('password123', { force: true })
    
    // Capture 2: Formulaire rempli
    cy.screenshot('2-form-filled')
    
    // Trouve et clique sur le bouton
    cy.get('button').each(($btn, index) => {
      cy.log(`Button ${index}:`, $btn.text().trim(), $btn.attr('type'), $btn.attr('class'))
    })
    
    cy.get('button[type="submit"], button:contains("Connexion"), button:contains("Login"), button:first')
      .first()
      .click({ force: true })
    
    // Capture 3: Immédiatement après click
    cy.screenshot('3-immediately-after-click')
    
    // Attend et capture l'état final
    cy.wait(5000)
    cy.screenshot('4-after-waiting')
    
    // Analyse finale
    cy.url().then(currentUrl => {
      cy.log('Final URL:', currentUrl)
    })
    
    cy.document().then(doc => {
      cy.log('Page title:', doc.title)
      const errorElements = doc.querySelectorAll('.error, .alert, [role="alert"], .text-danger')
      cy.log('Error elements found:', errorElements.length)
    })
  })
})
