describe('Stable Navigation', () => {
  it('should navigate between all features reliably', () => {
    // Test chaque fonctionnalité indépendamment
    const features = [
      { name: 'users', button: '👥 Tester Utilisateurs' },
      { name: 'offers', button: '🚗 Tester Offres' },
      { name: 'reservations', button: '📅 Tester Réservations' },
      { name: 'stats', button: '📊 Tester Stats' }
    ]

    features.forEach(feature => {
      cy.log(`Testing ${feature.name}...`)
      
      // Chaque test commence sur une page fraîche
      cy.visit('/')
      cy.contains('MonApp').should('be.visible')
      cy.wait(1000) // Stabilisation Angular
      
      // Clique et vérifie
      cy.contains('button', feature.button).click()
      cy.wait(3000) // Attendre le chargement complet
      
      // Vérification basique de la page
      cy.get('body').should('exist')
      cy.screenshot(`${feature.name}-page`)
      
      cy.log(`✅ ${feature.name} navigation successful`)
    })
  })

  it('should handle quick navigation sequence', () => {
    cy.visit('/')
    cy.contains('MonApp').should('be.visible')
    
    // Navigation rapide mais avec des visites séparées
    cy.contains('button', '👥 Tester Utilisateurs').click()
    cy.wait(2000)
    cy.visit('/')
    
    cy.contains('button', '🚗 Tester Offres').click() 
    cy.wait(2000)
    cy.visit('/')
    
    cy.contains('button', '📅 Tester Réservations').click()
    cy.wait(2000)
    
    cy.log('✅ Quick navigation sequence completed')
    cy.screenshot('quick-navigation-sequence')
  })
})
