export interface Alert {
  coordinates: firebase.firestore.GeoPoint;
  notes: string;
  requiredSkills: {
    AED: boolean,
    CPR: boolean,
    STOP_HEAVY_BLEEDING: boolean,
    TREATING_SHOCK: boolean
  };
  isExpired: boolean;
}
