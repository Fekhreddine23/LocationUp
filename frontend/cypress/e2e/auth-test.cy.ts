describe('Authentication Test', () => {
  it('should access login page', () => {
    cy.visit('/')
    
    // Essaie d'accéder à la page de login
    cy.visit('/login')
    
    // Vérifie qu'on est sur une page de login
    cy.url().should('include', 'login')
    
    // Cherche des champs de connexion
    cy.get('input[type="text"], input[type="email"], input[name="username"]').should('exist')
    cy.get('input[type="password"]').should('exist')
    cy.get('button[type="submit"]').should('exist')
    
    cy.screenshot('login-page')
  })
  
  it('should login with test user', () => {
    cy.visit('/login')
    
    // Utilise les identifiants de test
    cy.get('input[type="text"], input[type="email"], input[name="username"]').first().type('testuser')
    cy.get('input[type="password"]').first().type('password123')
    cy.get('button[type="submit"]').click()
    
    // Vérifie la redirection après login
    cy.url().should('not.include', 'login')
    cy.screenshot('after-login')
  })
})
