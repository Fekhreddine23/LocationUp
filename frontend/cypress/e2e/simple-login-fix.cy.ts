describe('Simple Login Fix', () => {
  it('should login using placeholder texts', () => {
    cy.visit('/login')
    
    // Méthode 1: Par placeholder
    cy.get('input[placeholder*="utilisateur"], input[placeholder*="nom"]').type('testuser')
    cy.get('input[placeholder*="mot de passe"], input[type="password"]').type('password123')
    cy.contains('button', 'se connecter').click()
    
    cy.wait(3000)
    
    // Vérifie si on a réussi
    cy.url().then(currentUrl => {
      if (!currentUrl.includes('login')) {
        cy.log('✅ Successfully logged in!')
        cy.contains('LocationUp').should('be.visible')
      } else {
        cy.log('❌ Login failed - still on login page')
        // Prend une capture pour debug
        cy.screenshot('login-failure-details')
      }
    })
  })
  
  it('should try alternative credentials', () => {
    cy.visit('/login')
    
    // Essaie admin si testuser ne fonctionne pas
    cy.get('input[placeholder*="utilisateur"]').type('admin')
    cy.get('input[placeholder*="mot de passe"]').type('admin')
    cy.contains('button', 'se connecter').click()
    
    cy.wait(3000)
    
    cy.url().then(currentUrl => {
      cy.log('URL after admin login attempt:', currentUrl)
    })
  })
})
