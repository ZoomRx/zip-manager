declare module '@capacitor/core' {
  interface PluginRegistry {
    ZipManager: ZipManagerPlugin;
  }
}

export interface ZipManagerPlugin {
  extract(options: { files: [string], destination: string }): Promise<void>;
  compress(options: { files: [string], destination: string }): Promise<void>;
}
