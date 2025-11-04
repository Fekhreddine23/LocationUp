describe('File Logger Debug', () => {
  it('should log form details to file', () => {
    cy.visit('/login')
    
    let debugInfo = '=== FORM DEBUG INFO ===\\n\\n'
    
    // Capture les informations des inputs
    cy.get('input').each(($input, index) => {
      const inputInfo = {
        index: index,
        type: $input.attr('type'),
        name: $input.attr('name'), 
        placeholder: $input.attr('placeholder'),
        id: $input.attr('id'),
        class: $input.attr('class')
      }
      debugInfo += `Input ${index}: ${JSON.stringify(inputInfo)}\\n`
    })
    
    debugInfo += '\\n=== BUTTONS ===\\n'
    
    // Capture les informations des boutons
    cy.get('button, input[type="submit"]').each(($btn, index) => {
      const buttonInfo = {
        index: index,
        text: $btn.text().trim(),
        type: $btn.attr('type'),
        id: $btn.attr('id'),
        class: $btn.attr('class')
      }
      debugInfo += `Button ${index}: ${JSON.stringify(buttonInfo)}\\n`
    })
    
    // Écrit dans un fichier
    cy.writeFile('cypress/debug-form-structure.txt', debugInfo)
    
    cy.log('Form structure saved to cypress/debug-form-structure.txt')
  })
})
