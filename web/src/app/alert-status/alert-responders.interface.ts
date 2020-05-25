export interface AlertResponders {
  respondersStatus: {
    [uid: string]: {
      knownLocation: firebase.firestore.GeoPoint;
      status: 'pending_location' | 'awaiting' | 'too_far' | 'ignored' | 'rejected' | 'accepted';
    }
  };
}
