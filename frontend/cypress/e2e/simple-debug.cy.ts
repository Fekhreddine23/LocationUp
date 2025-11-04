describe('Simple Debug', () => {
  it('should find any interactive elements', () => {
    cy.visit('/login')
    
    let debugInfo = '=== SIMPLE DEBUG ===\\n\\n'
    
    // Méthode simple: juste les inputs et boutons basiques
    cy.get('input, button').each(($el, index) => {
      const elementInfo = {
        tag: $el[0].tagName,
        type: $el.attr('type'),
        placeholder: $el.attr('placeholder'),
        name: $el.attr('name'),
        id: $el.attr('id'),
        class: $el.attr('class'),
        value: $el.attr('value'),
        text: $el.text().trim()
      }
      debugInfo += `Element ${index}: ${JSON.stringify(elementInfo)}\\n`
    }).then(() => {
      // Si aucun élément trouvé, essaie autre chose
      if (debugInfo === '=== SIMPLE DEBUG ===\\n\\n') {
        debugInfo += 'No standard input/button elements found\\n'
        debugInfo += 'Trying contenteditable elements...\\n'
        
        cy.get('[contenteditable="true"]').each(($el, index) => {
          debugInfo += `Contenteditable ${index}: ${$el.attr('class')} - "${$el.text()}"\\n`
        })
      }
      
      cy.writeFile('cypress/simple-debug.txt', debugInfo)
    })
    
    cy.screenshot('simple-debug-result')
  })
})
