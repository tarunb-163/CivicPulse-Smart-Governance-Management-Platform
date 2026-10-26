const fs = require('fs');
const path = require('path');

const staticDir = path.resolve(__dirname, '../src/main/resources/static');

function getAllHtmlFiles(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat && stat.isDirectory()) {
      results = results.concat(getAllHtmlFiles(fullPath));
    } else if (file.endsWith('.html')) {
      results.push(fullPath);
    }
  });
  return results;
}

const htmlFiles = getAllHtmlFiles(staticDir);
console.log(`Processing ${htmlFiles.length} HTML files...`);

let modifiedCount = 0;

htmlFiles.forEach(filePath => {
  let content = fs.readFileSync(filePath, 'utf8');
  let original = content;

  // WCAG 2.5.3 (Label in Name): accessible name must contain the visible string
  content = content.replace(/aria-label="Decrease font size"/g, 'aria-label="A- Decrease font size"');
  content = content.replace(/aria-label="Reset font size"/g, 'aria-label="A Reset font size"');
  content = content.replace(/aria-label="Increase font size"/g, 'aria-label="A+ Increase font size"');
  content = content.replace(/aria-label="Toggle Dark Mode"/g, 'aria-label="Theme: Toggle Dark Mode"');
  content = content.replace(/aria-label="CivicPulse Portal Home"/g, 'aria-label="CivicPulse Smart Governance Portal - Home"');

  if (content !== original) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`  Updated: ${path.relative(staticDir, filePath)}`);
    modifiedCount++;
  }
});

console.log(`Updated ${modifiedCount} HTML files successfully.`);
