import { Component, OnInit } from '@angular/core';
import { MapsService } from '../maps.service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { AngularFirestore } from '@angular/fire/firestore';
import { Router } from '@angular/router';
import * as firebase from 'firebase/app';
import 'firebase/firestore';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private mapsService: MapsService, private firestore: AngularFirestore, private router: Router) { }

  address: string;
  alertMessage: string;

  get markerPosition() {
    return this.alertForm.get('coordinates').value;
  }
  set markerPosition(coordinates: google.maps.LatLng) {
    this.alertForm.get('coordinates').setValue(coordinates);
  }
  center: google.maps.LatLng;
  mapZoomLevel = 6;

  displayInlineMap = false;

  selectedAddress: string;
  alertForm = new FormGroup({
    coordinates: new FormControl(null, [Validators.required]),
    requiredSkills: new FormGroup({
      AED: new FormControl(null),
      CPR: new FormControl(null),
      STOP_HEAVY_BLEEDING: new FormControl(null),
      TREATING_SHOCK: new FormControl(null)
    }),
    notes: new FormControl(null)
  });
  isSubmissionDisabled = false;


  ngOnInit(): void {
    this.center = new google.maps.LatLng(38.3, 23); // Center to greece

    this.markerPosition = new google.maps.LatLng(40.64874337, 22.961516);
    this.selectedAddress = 'Something do not chnage';
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
      this.markerPosition = position;
      this.mapsService.addressLookup(position).subscribe(resolvedAddress => {
        this.selectedAddress = resolvedAddress;
        this.mapZoomLevel = 16;
      });
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

  async onAlertSubmit() {
    if (this.alertForm.valid) {
      this.isSubmissionDisabled = true;
      const coordinates: google.maps.LatLngLiteral = this.alertForm.get('coordinates').value.toJSON();
      this.alertForm.get('coordinates').setValue(new firebase.firestore.GeoPoint(coordinates.lat, coordinates.lng));
      const alert = await this.firestore.collection('alerts').add(
        this.alertForm.value
      );
      this.router.navigate(['alert', alert.id]);
    }
  }

}
