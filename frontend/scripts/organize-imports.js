#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const ts = require('typescript');

function walk(dir) {
  const results = [];
  for (const name of fs.readdirSync(dir)) {
    const fp = path.join(dir, name);
    const stat = fs.statSync(fp);
    if (stat.isDirectory()) {
      if (name === 'node_modules' || name === 'dist' || name === '.git') continue;
      results.push(...walk(fp));
    } else if (stat.isFile() && (fp.endsWith('.ts') || fp.endsWith('.tsx'))) {
      results.push(fp);
    }
  }
  return results;
}

function classify(spec) {
  if (!spec) return 'external';
  if (spec.startsWith('.')) return 'relative';
  return 'external';
}

function organizeFile(filePath) {
  const text = fs.readFileSync(filePath, 'utf8');
  const sf = ts.createSourceFile(filePath, text, ts.ScriptTarget.Latest, true);

  const importNodes = [];
  for (const stmt of sf.statements) {
    const kind = stmt.kind;
    if (
      kind === ts.SyntaxKind.ImportDeclaration ||
      kind === ts.SyntaxKind.ImportEqualsDeclaration ||
      (kind === ts.SyntaxKind.ExportDeclaration && stmt.moduleSpecifier)
    ) {
      importNodes.push(stmt);
    } else {
      break; // stop at first non-import/export-with-moduleSpecifier
    }
  }

  if (importNodes.length <= 1) return false; // nothing to reorder

  const first = importNodes[0];
  const last = importNodes[importNodes.length - 1];
  const prefix = text.slice(0, first.getStart(sf));

  const groups = { external: [], relative: [] };

  for (const node of importNodes) {
    let spec = '';
    if (node.moduleSpecifier && node.moduleSpecifier.text !== undefined) {
      spec = node.moduleSpecifier.text;
    } else if (node.moduleReference && node.moduleReference.expression && node.moduleReference.expression.text) {
      spec = node.moduleReference.expression.text;
    }
    const raw = text.slice(node.getStart(sf), node.getEnd());
    const g = classify(spec);
    groups[g].push({ spec, raw });
  }

  const sortFn = (a, b) => {
    if (a.spec < b.spec) return -1;
    if (a.spec > b.spec) return 1;
    if (a.raw < b.raw) return -1;
    if (a.raw > b.raw) return 1;
    return 0;
  };

  groups.external.sort(sortFn);
  groups.relative.sort(sortFn);

  const rebuiltParts = [];
  if (groups.external.length) {
    rebuiltParts.push(groups.external.map(x => x.raw).join('\n'));
  }
  if (groups.relative.length) {
    if (groups.external.length) rebuiltParts.push(''); // blank line between groups
    rebuiltParts.push(groups.relative.map(x => x.raw).join('\n'));
  }

  const rebuilt = rebuiltParts.join('\n');
  const rest = text.slice(last.getEnd());

  const newText = prefix + rebuilt + rest;
  if (newText !== text) {
    fs.writeFileSync(filePath, newText, 'utf8');
    return true;
  }
  return false;
}

function main() {
  const base = path.join(__dirname, '..', 'src');
  if (!fs.existsSync(base)) {
    console.error('src/ not found, aborting');
    process.exit(1);
  }

  const files = walk(base);
  let changed = 0;
  for (const f of files) {
    try {
      if (organizeFile(f)) {
        console.log('Updated', f);
        changed++;
      }
    } catch (err) {
      console.error('Error processing', f, err.message);
    }
  }

  console.log(`Done. Files updated: ${changed}`);
}

main();
