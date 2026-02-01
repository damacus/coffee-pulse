// Wrapper for the Navigator Wake Lock API

class WakeLockService {
  private wakeLock: WakeLockSentinel | null = null;

  public async request() {
    try {
      if ('wakeLock' in navigator) {
        this.wakeLock = await navigator.wakeLock.request('screen');
        // Re-acquire lock if visibility changes (e.g. user tabs out and back)
        document.addEventListener('visibilitychange', this.handleVisibilityChange);
      }
    } catch (err) {
      console.warn('Wake Lock request failed:', err);
    }
  }

  public async release() {
    if (this.wakeLock) {
      try {
        await this.wakeLock.release();
        this.wakeLock = null;
        document.removeEventListener('visibilitychange', this.handleVisibilityChange);
      } catch (err) {
        console.warn('Wake Lock release failed:', err);
      }
    }
  }

  private handleVisibilityChange = async () => {
    if (this.wakeLock !== null && document.visibilityState === 'visible') {
      await this.request();
    }
  };
}

export const wakeLockService = new WakeLockService();
