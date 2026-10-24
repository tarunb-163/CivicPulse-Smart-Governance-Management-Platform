const fs = require('fs');
const path = require('path');

// 16x16 32-bit RGBA BMP icon in ICO container
const width = 16;
const height = 16;
const bpp = 32;
const imageSize = width * height * 4;
const maskSize = Math.ceil(width / 32) * 4 * height; // 16 bits padded to 32 bits = 4 bytes * 16 = 64
const dibHeaderSize = 40;
const totalImageDataSize = dibHeaderSize + imageSize + maskSize;

const icoHeader = Buffer.alloc(6);
icoHeader.writeUInt16LE(0, 0); // reserved
icoHeader.writeUInt16LE(1, 2); // ICO
icoHeader.writeUInt16LE(1, 4); // 1 image

const icoDir = Buffer.alloc(16);
icoDir.writeUInt8(width, 0);
icoDir.writeUInt8(height, 1);
icoDir.writeUInt8(0, 2); // palette
icoDir.writeUInt8(0, 3); // reserved
icoDir.writeUInt16LE(1, 4); // color planes
icoDir.writeUInt16LE(bpp, 6); // bpp
icoDir.writeUInt32LE(totalImageDataSize, 8); // size of image data
icoDir.writeUInt32LE(22, 12); // offset (6 + 16)

const dibHeader = Buffer.alloc(dibHeaderSize);
dibHeader.writeUInt32LE(dibHeaderSize, 0);
dibHeader.writeInt32LE(width, 4);
dibHeader.writeInt32LE(height * 2, 8); // height is double for mask in ICO
dibHeader.writeUInt16LE(1, 12); // planes
dibHeader.writeUInt16LE(bpp, 14); // 32 bpp
dibHeader.writeUInt32LE(0, 16); // BI_RGB
dibHeader.writeUInt32LE(imageSize + maskSize, 20); // image size
dibHeader.writeInt32LE(0, 24);
dibHeader.writeInt32LE(0, 28);
dibHeader.writeUInt32LE(0, 32);
dibHeader.writeUInt32LE(0, 36);

const pixels = Buffer.alloc(imageSize);
// Fill with civic blue #2563EB (BGRA format)
for (let i = 0; i < width * height; i++) {
  pixels[i * 4 + 0] = 235; // B
  pixels[i * 4 + 1] = 99;  // G
  pixels[i * 4 + 2] = 37;  // R
  pixels[i * 4 + 3] = 255; // A
}

const mask = Buffer.alloc(maskSize, 0); // fully opaque

const icoBuffer = Buffer.concat([icoHeader, icoDir, dibHeader, pixels, mask]);
const targetPath = path.resolve(__dirname, '../src/main/resources/static/favicon.ico');
fs.writeFileSync(targetPath, icoBuffer);
console.log('Created valid favicon.ico at:', targetPath, 'size:', icoBuffer.length);
