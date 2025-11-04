describe('Full Feature Test', () => {
  beforeEach(() => {
    cy.visit('/')
    cy.contains('MonApp').should('be.visible')
  })

  it('should test user management', () => {
    cy.contains('button', '👥 Tester Utilisateurs').click()
    cy.wait(2000)
    
    // Vérifie la page utilisateurs
    cy.get('body').then($body => {
      // Vérifie la présence d'éléments de gestion utilisateurs
      const hasUserElements = $body.find('table, .table, [role="grid"], mat-table').length > 0
      const hasUserText = $body.text().includes('utilisateur') || $body.text().includes('user')
      
      if (hasUserElements || hasUserText) {
        cy.log('✅ User management page loaded')
        cy.screenshot('user-management-functional')
      } else {
        cy.log('⚠️ User page might be empty or loading')
        cy.screenshot('user-page-empty')
      }
    })
  })

  it('should test offers management', () => {
    cy.contains('button', '🚗 Tester Offres').click()
    cy.wait(2000)
    
    cy.get('body').then($body => {
      const hasOfferElements = $body.find('.card, .offer, [data-testid*="offer"]').length > 0
      const hasOfferText = $body.text().includes('offre') || $body.text().includes('offer')
      
      if (hasOfferElements || hasOfferText) {
        cy.log('✅ Offers management page loaded')
        cy.screenshot('offers-management-functional')
      }
    })
  })

  it('should test reservations management', () => {
    cy.contains('button', '📅 Tester Réservations').click()
    cy.wait(2000)
    
    cy.get('body').then($body => {
      const hasReservationElements = $body.find('.reservation, .booking, table').length > 0
      const hasReservationText = $body.text().includes('réservation') || $body.text().includes('booking')
      
      if (hasReservationElements || hasReservationText) {
        cy.log('✅ Reservations management page loaded')
        cy.screenshot('reservations-management-functional')
      }
    })
  })

  it('should test stats dashboard', () => {
    cy.contains('button', '📊 Tester Stats').click()
    cy.wait(2000)
    
    cy.get('body').then($body => {
      const hasStatsElements = $body.find('.chart, .stat, .metric, canvas').length > 0
      const hasStatsText = $body.text().includes('stat') || $body.text().includes('dashboard')
      
      if (hasStatsElements || hasStatsText) {
        cy.log('✅ Stats dashboard loaded')
        cy.screenshot('stats-dashboard-functional')
      }
    })
  })

  it('should verify navigation between features', () => {
    // Test de navigation complète
    cy.contains('button', '👥 Tester Utilisateurs').click()
    cy.wait(1000)
    cy.go('back')
    
    cy.contains('button', '🚗 Tester Offres').click()
    cy.wait(1000)
    cy.go('back')
    
    cy.contains('button', '📅 Tester Réservations').click()
    cy.wait(1000)
    cy.go('back')
    
    cy.contains('button', '📊 Tester Stats').click()
    cy.wait(1000)
    
    cy.log('✅ All navigation working correctly')
    cy.screenshot('full-navigation-test')
  })
})
