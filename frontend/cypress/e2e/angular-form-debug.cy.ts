describe('Angular Form Debug', () => {
  it('should find angular form elements', () => {
    cy.visit('/login')
    
    let debugInfo = '=== ANGULAR FORM DEBUG ===\\n\\n'
    
    // Cherche tous les éléments interactifs
    debugInfo += '=== ALL INTERACTIVE ELEMENTS ===\\n'
    
    // Tous les éléments focusables
    cy.get('input, button, [tabindex], [role="button"], [ng-reflect-*]').each(($el, index) => {
      const elementInfo = {
        tag: $el[0].tagName,
        type: $el.attr('type'),
        placeholder: $el.attr('placeholder'),
        'ng-*': Object.keys($el[0].attributes)
          .map(key => $el[0].attributes[key].name)
          .filter(attr => attr.startsWith('ng-') || attr.startsWith('_ng') || attr.includes('reflect'))
          .join(', '),
        class: $el.attr('class'),
        text: $el.text().trim().substring(0, 50)
      }
      debugInfo += `Element ${index}: ${JSON.stringify(elementInfo)}\\n`
    })
    
    // Écrit le debug info
    cy.writeFile('cypress/debug-angular-form.txt', debugInfo)
    cy.log('Angular form structure saved')
    
    // Prend une capture
    cy.screenshot('angular-form-debug')
  })
})
