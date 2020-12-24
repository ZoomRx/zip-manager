declare module '@capacitor/core' {
  interface PluginRegistry {
    ZipManager: ZipManagerPlugin;
  }
}

export interface ZipManagerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
