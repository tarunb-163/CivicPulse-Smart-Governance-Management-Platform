/**
 * CivicPulse Test Data Factory
 * Provides pre-configured, randomized and boundary data payloads for tests
 */

const path = require('path');
const fs = require('fs');

const FIXTURES_DIR = path.resolve(__dirname, '../fixtures');
if (!fs.existsSync(FIXTURES_DIR)) {
  fs.mkdirSync(FIXTURES_DIR, { recursive: true });
}

// Generate simple 1x1 PNG buffer for upload tests
function getTestImageBuffer(filename = 'test-evidence.png') {
  const filePath = path.join(FIXTURES_DIR, filename);
  if (!fs.existsSync(filePath)) {
    // 1x1 transparent PNG buffer
    const pngBuffer = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==', 'base64');
    fs.writeFileSync(filePath, pngBuffer);
  }
  return filePath;
}

function randomSuffix() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

const DataFactory = {
  // Generate unique citizen user for registration tests
  uniqueUser(overrides = {}) {
    const id = randomSuffix();
    return {
      fullName: `Test Citizen ${id}`,
      name: `Test Citizen ${id}`,
      email: `citizen_${id}@civicpulse.test`,
      phone: `98${Math.floor(10000000 + Math.random() * 90000000)}`,
      password: 'Password@123',
      ...overrides
    };
  },

  // Standard Valid Grievance
  validComplaint(overrides = {}) {
    const id = randomSuffix();
    return {
      title: `Pothole on MG Road near Junction ${id}`,
      description: `Large crater-like pothole causing heavy traffic slowdown and risk of two-wheeler accidents near junction ${id}.`,
      categoryId: 1,
      category: 'Pothole & Road Damage',
      urgency: 'HIGH',
      address: 'MG Road, Ward 12, Davanagere, Karnataka',
      latitude: 14.4644,
      longitude: 75.9218,
      anonymous: false,
      images: [getTestImageBuffer('pothole-1.png')],
      ...overrides
    };
  },

  // Grievance with 3 Images
  complaintWith3Images(overrides = {}) {
    const id = randomSuffix();
    return {
      title: `Garbage Dump Overflow on Ring Road ${id}`,
      description: `Massive uncollected garbage dump overflowing onto main road spreading foul smell and flies near colony ${id}.`,
      category: 'Garbage Dump / Overflowing Bin',
      urgency: 'MEDIUM',
      address: 'Ring Road, Sector 4, Davanagere',
      latitude: 14.4690,
      longitude: 75.9250,
      anonymous: false,
      images: [
        getTestImageBuffer('garbage-1.png'),
        getTestImageBuffer('garbage-2.png'),
        getTestImageBuffer('garbage-3.png')
      ],
      ...overrides
    };
  },

  // Anonymous Grievance
  anonymousComplaint(overrides = {}) {
    const id = randomSuffix();
    return {
      title: `Illegal Commercial Encroachment on Footpath ${id}`,
      description: `Shop vendor has constructed unauthorized permanent shed on pedestrian footpath blocking commuters.`,
      category: 'Footpath Vendor Encroachment',
      urgency: 'MEDIUM',
      address: 'Station Road, Davanagere',
      latitude: 14.4610,
      longitude: 75.9180,
      anonymous: true,
      images: [getTestImageBuffer('encroach-1.png')],
      ...overrides
    };
  },

  // Exact Duplicate
  exactDuplicateComplaint(base = this.validComplaint()) {
    return {
      ...base,
      // Same normalized content hash
      title: base.title,
      description: base.description,
      confirmNotDuplicate: false
    };
  },

  // Near Duplicate (Within proximity radius)
  nearDuplicateComplaint(base = this.validComplaint()) {
    return {
      title: `${base.title} nearby issue`,
      description: `${base.description} affecting the adjoining street.`,
      category: base.category,
      urgency: base.urgency,
      address: base.address,
      latitude: (base.latitude || 14.4644) + 0.0002, // approx 22 meters
      longitude: (base.longitude || 75.9218) + 0.0002,
      anonymous: false,
      confirmNotDuplicate: false
    };
  },

  // Multilingual / Unicode / Emoji
  multilingualComplaint(lang = 'kn') {
    const id = randomSuffix();
    if (lang === 'kn') {
      return {
        title: `ರಸ್ತೆ ಗುಂಡಿ ದುರಸ್ತಿ ಬೇಕಾಗಿದೆ ${id}`,
        description: `ಮುಖ್ಯ ರಸ್ತೆಯಲ್ಲಿ ದೊಡ್ಡ ಗುಂಡಿ ಬಿದ್ದಿದ್ದು ವಾಹನ ಸಂಚಾರಕ್ಕೆ ತೊಂದರೆಯಾಗಿದೆ. ದಯವಿಟ್ಟು ಕೂಡಲೇ ದುರಸ್ತಿ ಮಾಡಿ. ⚠️🚧`,
        category: 'Pothole & Road Damage',
        urgency: 'HIGH',
        address: 'ವಿದ್ಯಾನಗರ, ದಾವಣಗೆರೆ',
        latitude: 14.4650,
        longitude: 75.9230,
        anonymous: false
      };
    } else if (lang === 'hi') {
      return {
        title: `सड़क पर बड़ा गड्ढा और जलभराव ${id}`,
        description: `बस स्टैंड के पास सड़क पर बहुत बड़ा गड्ढा हो गया है जिससे पानी भर गया है और दुर्घटना का खतरा है। ⚠️🚸`,
        category: 'Pothole & Road Damage',
        urgency: 'HIGH',
        address: 'पीबी रोड, दावणगेरे',
        latitude: 14.4650,
        longitude: 75.9230,
        anonymous: false
      };
    }
    return this.validComplaint({ title: `Civic issue with emojis 🚰⚡⚠️ ${id}` });
  },

  // 10,000 Character Text
  tenKCharComplaint() {
    const id = randomSuffix();
    const sentence = "Detailed municipal civic complaint inspection report particulars and citizen observation notes. ";
    const description = sentence.repeat(100).slice(0, 9900) + ` [End of Report ${id}]`;
    return {
      title: `Comprehensive Infrastructure Assessment Report ${id}`,
      description: description,
      category: 'Pothole & Road Damage',
      urgency: 'LOW',
      address: 'Main Administrative Corridor, Davanagere',
      latitude: 14.4644,
      longitude: 75.9218,
      anonymous: false
    };
  },

  getTestImage: getTestImageBuffer
};

module.exports = { DataFactory };
