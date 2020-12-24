import { WebPlugin } from '@capacitor/core';
import { ZipManagerPlugin } from './definitions';

export class ZipManagerWeb extends WebPlugin implements ZipManagerPlugin {
  constructor() {
    super({
      name: 'ZipManager',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const ZipManager = new ZipManagerWeb();

export { ZipManager };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(ZipManager);
