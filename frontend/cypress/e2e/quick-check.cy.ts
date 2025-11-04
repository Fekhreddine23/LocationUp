describe('Quick Check', () => {
  it('should quickly test if buttons work', () => {
    cy.visit('/')
    
    // Vérifie la page d'accueil
    cy.contains('MonApp').should('be.visible')
    cy.log('✅ Homepage loaded')
    
    // Teste un bouton rapidement
    cy.contains('button', 'Tester Utilisateurs').click()
    cy.wait(3000)
    
    // Vérifie le résultat
    cy.url().then(url => {
      cy.log('Current URL:', url)
      
      if (url.includes('login')) {
        cy.log('❌ Redirected to login page')
        cy.screenshot('login-redirect')
      } else {
        cy.log('✅ Direct access to feature')
        cy.screenshot('feature-access')
      }
    })
  })
})
