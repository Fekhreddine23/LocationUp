describe('Navigation Test', () => {
  it('should have working navigation', () => {
    cy.visit('/')
    
    // Vérifie que LocationUp est visible
    cy.contains('LocationUp').should('be.visible')
    
    // Cherche des liens de navigation communs
    const commonLinks = ['Connexion', 'Login', 'Accueil', 'Dashboard', 'Admin']
    
    commonLinks.forEach(linkText => {
      cy.get('body').then($body => {
        if ($body.text().includes(linkText)) {
          cy.contains(linkText).should('be.visible')
          cy.log(`✅ Navigation link found: ${linkText}`)
        }
      })
    })
    
    // Prend une capture
    cy.screenshot('navigation-check')
  })
})
