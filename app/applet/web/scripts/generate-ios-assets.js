const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

async function generateAssets() {
  const rootDir = path.resolve(__dirname, '..');
  const logoSvgPath = path.join(rootDir, 'logo.svg');
  const appIconDir = path.join(rootDir, 'ios', 'App', 'App', 'Assets.xcassets', 'AppIcon.appiconset');
  const splashDir = path.join(rootDir, 'ios', 'App', 'App', 'Assets.xcassets', 'Splash.imageset');

  console.log('Generating iOS App Icon (1024x1024)...');
  const svgBuffer = fs.readFileSync(logoSvgPath);

  // AppIcon-512@2x.png (1024x1024)
  await sharp(svgBuffer)
    .resize(1024, 1024)
    .png()
    .toFile(path.join(appIconDir, 'AppIcon-512@2x.png'));
  console.log('✓ AppIcon-512@2x.png created');

  // Splash screen (2732x2732) - Deep navy #071426 background with centered logo (approx 680x680)
  console.log('Generating iOS Splash Screen (2732x2732)...');
  const logoResized = await sharp(svgBuffer)
    .resize(720, 720)
    .png()
    .toBuffer();

  const splash = await sharp({
    create: {
      width: 2732,
      height: 2732,
      channels: 4,
      background: { r: 7, g: 20, b: 38, alpha: 1 } // #071426
    }
  })
  .composite([
    {
      input: logoResized,
      gravity: 'center'
    }
  ])
  .png()
  .toBuffer();

  fs.writeFileSync(path.join(splashDir, 'splash-2732x2732.png'), splash);
  fs.writeFileSync(path.join(splashDir, 'splash-2732x2732-1.png'), splash);
  fs.writeFileSync(path.join(splashDir, 'splash-2732x2732-2.png'), splash);
  console.log('✓ iOS Splash Screen assets created');
}

generateAssets().catch(err => {
  console.error(err);
  process.exit(1);
});
