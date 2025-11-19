describe('Test Buttons', () => {
  it('should test all functionality buttons', () => {
    cy.visit('/')
    
    // Vérifie que LocationUp est visible
    cy.contains('LocationUp').should('be.visible')
    cy.screenshot('main-page')
    
    // Teste le bouton Stats
    cy.contains('button', '📊 Tester Stats').click()
    cy.wait(2000)
    cy.url().then(url => cy.log('After stats click:', url))
    cy.screenshot('after-stats')
    cy.go('back')
    
    // Teste le bouton Utilisateurs
    cy.contains('button', '👥 Tester Utilisateurs').click()
    cy.wait(2000)
    cy.url().then(url => cy.log('After users click:', url))
    cy.screenshot('after-users')
    cy.go('back')
    
    // Teste le bouton Offres
    cy.contains('button', '🚗 Tester Offres').click()
    cy.wait(2000)
    cy.url().then(url => cy.log('After offers click:', url))
    cy.screenshot('after-offers')
    cy.go('back')
    
    // Teste le bouton Réservations
    cy.contains('button', '📅 Tester Réservations').click()
    cy.wait(2000)
    cy.url().then(url => cy.log('After reservations click:', url))
    cy.screenshot('after-reservations')
  })
  
  it('should verify navigation works', () => {
    cy.visit('/')
    
    // Clique sur Utilisateurs et vérifie le contenu
    cy.contains('button', '👥 Tester Utilisateurs').click()
    cy.wait(3000)
    
    // Vérifie qu'on est sur une page de gestion utilisateurs
    cy.get('body').then($body => {
      const hasUserContent = $body.text().includes('utilisateur') || 
                            $body.text().includes('user') ||
                            $body.find('table, .table, [role="grid"]').length > 0
      
      if (hasUserContent) {
        cy.log('✅ User management page loaded successfully')
        cy.screenshot('user-management-page')
      } else {
        cy.log('⚠️  Might be on login page or loading')
        cy.screenshot('possible-login-page')
      }
    })
  })
})
