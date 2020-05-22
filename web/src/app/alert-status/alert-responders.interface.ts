export interface AlertResponders {
  respondersStatus: {
    [uid: string]: {
      knownLocation: firebase.firestore.GeoPoint;
      status: 'pending_location' | 'awaiting' | 'too_far' | 'not_needed'  | 'rejected' | 'accepted';
    }
  };
}
