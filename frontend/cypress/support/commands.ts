/// <reference types="cypress" />

// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

// -- This is a parent command --
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.visit('/login')
  cy.get('input[type="text"], input[name="username"]').first().type(username)
  cy.get('input[type="password"], input[name="password"]').first().type(password)
  cy.get('button[type="submit"]').click()
})

// Version simplifiée sans session pour l'instant
Cypress.Commands.add('loginWithSession', (username: string, password: string) => {
  cy.visit('/login')
  cy.get('input[type="text"], input[name="username"]').first().type(username)
  cy.get('input[type="password"], input[name="password"]').first().type(password)
  cy.get('button[type="submit"]').click()
})
