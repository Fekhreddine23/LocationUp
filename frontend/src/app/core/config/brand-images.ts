export type BrandImageKey = 'clio' | 'renault' | 'peugeot' | 'dacia' | 'toyota' | 'citroen' | 'nissan' | 'kangoo' | 'tesla';

export interface BrandImageData {
  keywords: string[];
  gallery: string[];
  card?: string;
}

export const BRAND_IMAGE_MAP: Record<BrandImageKey, BrandImageData> = {
  clio: {
    keywords: ['clio', 'renault'],
    gallery: [
      'assets/images/clio/clio1.jpg',
      'assets/images/clio/clio2.jpg',
      'assets/images/clio/clio3.jpg'
    ],
    card: 'assets/images/clio/clio1.jpg'
  },
  renault: {
    keywords: ['renault'],
    gallery: [
      'assets/images/clio/clio1.jpg',
      'assets/images/clio/clio2.jpg',
      'assets/images/clio/clio3.jpg'
    ],
    card: 'assets/images/clio/clio1.jpg'
  },
  peugeot: {
    keywords: ['peugeot'],
    gallery: [
      'assets/images/peugeot/peugeot1.jpg',
      'assets/images/peugeot/peugeot2.jpg',
      'assets/images/peugeot/peugeot3.jpg'
    ],
    card: 'assets/images/peugeot/peugeot3.jpg'
  },
  dacia: {
    keywords: ['dacia', 'logan', 'sandero'],
    gallery: [
      'assets/images/dacia/dacia1.jpg',
      'assets/images/dacia/dacia2.jpg',
      'assets/images/dacia/dacia3.jpg'
    ],
    card: 'assets/images/dacia/dacia3.jpg'
  },
  toyota: {
    keywords: ['toyota', 'yaris', 'corolla', 'rav4'],
    gallery: [
      'assets/images/toyota/toyota1.jpg',
      'assets/images/toyota/toyota2.jpg',
      'assets/images/toyota/toyota3.jpg'
    ],
    card: 'assets/images/toyota/toyota3.jpg'
  },
  citroen: {
    keywords: ['citroen', 'c3', 'c4', 'ds3', 'ds4'],
    gallery: [
      'assets/images/citroen/citroen1.jpg',
      'assets/images/citroen/citroen2.jpg',
      'assets/images/citroen/citroen3.jpg'
    ],
    card: 'assets/images/citroen/citroen2.jpg'
  }
  ,
  nissan: {
    keywords: ['nissan', 'leaf', 'micra'],
    gallery: [
      'assets/images/nissan/nissan1.jpg',
      'assets/images/nissan/nissan2.jpg',
      'assets/images/nissan/nissan3.jpg'
    ],
    card: 'assets/images/nissan/nissan3.jpg'
  },
  kangoo: {
    keywords: ['kangoo'],
    gallery: [
      'assets/images/kangoo/kangoo1.jpg',
      'assets/images/kangoo/kangoo2.jpg',
      'assets/images/kangoo/kangoo3.jpg'
    ],
    card: 'assets/images/kangoo/kangoo1.jpg'
  },
  tesla: {
    keywords: ['tesla', 'model'],
    gallery: [
      'assets/images/tesla/tesla1.jpg',
      'assets/images/tesla/tesla2.jpg',
      'assets/images/tesla/tesla3.jpg'
    ],
    card: 'assets/images/tesla/tesla1.jpg'
  }
};

export function findBrandKey(text: string): BrandImageKey | null {
  const normalized = text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
  const normalizedCollapsed = normalized.replace(/[\s-]/g, '');
  const priority: BrandImageKey[] = [
    'kangoo',
    'clio',
    'peugeot',
    'citroen',
    'nissan',
    'tesla',
    'dacia',
    'toyota',
    'renault'
  ];

  for (const brand of priority) {
    const meta = BRAND_IMAGE_MAP[brand];
    if (meta?.keywords.some(keyword => {
      const key = keyword
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();
      return normalized.includes(key) || normalizedCollapsed.includes(key.replace(/\s+/g, ''));
    })) {
      return brand;
    }
  }

  return null;
}
