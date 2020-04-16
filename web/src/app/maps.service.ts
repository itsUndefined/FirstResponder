import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class MapsService {

  geocoder: google.maps.Geocoder;

  constructor(private ngZone: NgZone) {
    this.geocoder = new google.maps.Geocoder();
  }

  geocode(address: string): Observable<google.maps.LatLng> {
    return new Observable(subscriber => {
      this.geocoder.geocode({
        address,
        componentRestrictions: {
          country: 'GR'
        }
      }, (response, status) => {
        if (status === google.maps.GeocoderStatus.OK) {
          subscriber.next(response[0].geometry.location);
        } else if (status === google.maps.GeocoderStatus.ZERO_RESULTS) {
          subscriber.next(null);
        } else {
          subscriber.error(status);
        }
        subscriber.complete();
      });
    });
  }

  addressLookup(coords: google.maps.LatLng): Observable<string> {
    return new Observable(subscriber => {
      this.geocoder.geocode({
        location: coords
      }, (response, status) => {
        if (status === google.maps.GeocoderStatus.OK) {
          console.log(response[0]);
          subscriber.next(response[0].formatted_address);
          for (const component of response[0].address_components) {
            if (component.types.indexOf('country') !== -1 && component.short_name !== 'GR') {
              subscriber.next(null);
              subscriber.complete();
              return;
            }
          }
        } else if (status === google.maps.GeocoderStatus.ZERO_RESULTS) {
          subscriber.next(null);
        } else {
          subscriber.error(status);
        }
        subscriber.complete();
      });
    });
  }
}
