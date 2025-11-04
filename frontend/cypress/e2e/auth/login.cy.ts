describe('Authentication Flow', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('should display login page', () => {
    cy.contains('Connexion').should('be.visible')
    cy.get('input[type="text"], input[name="username"], #username').first().should('be.visible')
    cy.get('input[type="password"], input[name="password"], #password').first().should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
  })

  it('should login successfully with test credentials', () => {
    // Utilise des sélecteurs plus flexibles
    cy.get('input[type="text"], input[name="username"], #username').first().type('testuser')
    cy.get('input[type="password"], input[name="password"], #password').first().type('password123')
    cy.get('button[type="submit"]').click()
    
    // Vérifie la redirection - adapte selon ton app
    cy.url().should('match', /\/(dashboard|home|admin|offers)/)
    
    // Vérifie un élément qui devrait être visible après login
    cy.get('body').should(($body) => {
      // Vérifie la présence d'au moins un de ces éléments
      expect($body.find('[data-testid="welcome"], .dashboard, h1, .navbar').length).to.be.greaterThan(0)
    })
  })

  it('should show error with invalid credentials', () => {
    cy.get('input[type="text"], input[name="username"], #username').first().type('wronguser')
    cy.get('input[type="password"], input[name="password"], #password').first().type('wrongpass')
    cy.get('button[type="submit"]').click()
    
    // Vérifie qu'un message d'erreur apparaît
    cy.get('.error, .alert, .message, [role="alert"], .text-danger', { timeout: 10000 })
      .should('be.visible')
      .and('contain', 'erreur', { matchCase: false })
  })
})