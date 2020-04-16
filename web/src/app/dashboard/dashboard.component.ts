import { Component, OnInit } from '@angular/core';
import { MapsService } from '../maps.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private mapsService: MapsService) { }

  center: google.maps.LatLng;

  address: string;
  alertMessage: string;

  // tslint:disable-next-line: variable-name
  markerPosition: google.maps.LatLng;

  selectedAddress: string;

  ngOnInit(): void {
    this.center = new google.maps.LatLng(38.3, 23);
  }

  onFindAddress() {
    this.mapsService.geocode(this.address).subscribe(position => {
      if (!position) {
        this.alertMessage = 'Address not found. Try something else.';
        setTimeout(() => {
          this.alertMessage = null;
        }, 5000);
        return;
      }
      this.center = position;
      this.markerPosition = this.center;
      this.mapsService.addressLookup(position).subscribe(resolvedAddress => this.selectedAddress = resolvedAddress);
    });
  }

  onAlertClose() {
    this.alertMessage = null;
  }

  onDragMarker(event: {latLng: google.maps.LatLng}) {
    this.markerPosition = event.latLng;
  }

  onDragMarkerEnd(event: {latLng: google.maps.LatLng}) {
    this.mapsService.addressLookup(event.latLng).subscribe(resolvedAddress => this.selectedAddress = resolvedAddress);
  }

  onAddMarker(event: {latLng: google.maps.LatLng}) {
    this.markerPosition = event.latLng;
    this.mapsService.addressLookup(event.latLng).subscribe(resolvedAddress => this.selectedAddress = resolvedAddress);
  }

}
