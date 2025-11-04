describe('Error Handling Login', () => {
  it('should handle login with error capture', () => {
    cy.visit('/login')
    
    // Remplit le formulaire
    cy.get('input').first().type('testuser')
    cy.get('input').eq(1).type('password123')
    
    // Intercepte les requêtes réseau pour debug
    cy.intercept('POST', '**/auth/login').as('loginRequest')
    cy.intercept('**/api/**').as('apiCalls')
    
    cy.contains('button', 'se connecter').click()
    
    // Attend les requêtes
    cy.wait('@loginRequest', { timeout: 5000 }).then((interception) => {
      if (interception) {
        cy.log('Login request made:', interception.request.body)
        cy.log('Login response:', interception.response?.status, interception.response?.body)
      }
    }).catch(() => {
      cy.log('No login API call detected - might be frontend validation')
    })
    
    // Capture l'état final
    cy.wait(2000)
    cy.screenshot('final-state')
    
    // Vérifie la console pour les erreurs JS
    cy.window().then((win) => {
      const consoleErrors = []
      cy.stub(win.console, 'error').callsFake((...args) => {
        consoleErrors.push(args)
        cy.log('Console error:', args)
      })
    })
  })
})
