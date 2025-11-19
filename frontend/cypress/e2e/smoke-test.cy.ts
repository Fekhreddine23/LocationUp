describe('Smoke Test', () => {
  it('should load the application', () => {
    cy.visit('/')
    cy.contains('LocationUp').should('be.visible')
  })
})
