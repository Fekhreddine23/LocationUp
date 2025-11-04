/// <reference types="cypress" />

declare namespace Cypress {
  interface Chainable {
    /**
     * Custom command to login
     * @example cy.login('username', 'password')
     */
    login(username: string, password: string): Chainable<void>
    
    /**
     * Custom command to login with session
     * @example cy.loginWithSession('username', 'password') 
     */
    loginWithSession(username: string, password: string): Chainable<void>
  }
}
