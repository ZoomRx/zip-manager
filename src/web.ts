import { WebPlugin } from '@capacitor/core';
import { ZipManagerPlugin } from './definitions';

export class ZipManagerWeb extends WebPlugin implements ZipManagerPlugin {
  constructor() {
    super({
      name: 'ZipManager',
      platforms: ['web'],
    });
  }

  async extract(_options: { files: [string], destination: string }): Promise<void> {
    return new Promise((_resolve, reject) => reject);
  }

  async compress(_options: { files: [string], destination: string }): Promise<void> {
    return new Promise((_resolve, reject) => reject);
  }
}

const ZipManager = new ZipManagerWeb();

export { ZipManager };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ZipManager);
