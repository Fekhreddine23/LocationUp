describe('Angular Stable Test', () => {
  it('should handle angular re-rendering properly', () => {
    cy.visit('/')
    
    // Attendre qu'Angular soit complètement chargé
    cy.window().should('have.property', 'ng')
    cy.wait(1500)
    
    const testButtons = [
      '👥 Tester Utilisateurs',
      '🚗 Tester Offres', 
      '📅 Tester Réservations',
      '📊 Tester Stats'
    ]
    
    testButtons.forEach(buttonText => {
      cy.log(`Testing: ${buttonText}`)
      
      // Recharger la page pour être dans un état propre
      cy.visit('/')
      cy.wait(1500)
      
      // Cliquer sur le bouton avec gestion d'erreur
      cy.contains('button', buttonText).then($btn => {
        if ($btn.length > 0) {
          cy.wrap($btn).click({ force: true })
          cy.wait(2500) // Attendre le re-rendering Angular
          cy.screenshot(`after-${buttonText.replace(' ', '-')}`)
        }
      })
    })
    
    cy.log('✅ All Angular navigation tests completed')
  })
})
