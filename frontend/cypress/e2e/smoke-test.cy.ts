describe('Smoke Test', () => {
  it('should load the application', () => {
    cy.visit('/')
    cy.contains('MonApp').should('be.visible')
  })
})
