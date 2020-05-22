import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AngularFirestore } from '@angular/fire/firestore';
import { Subscription } from 'rxjs';
import { AlertResponders } from './alert-responders.interface';

@Component({
  selector: 'app-alert-status',
  templateUrl: './alert-status.component.html',
  styleUrls: ['./alert-status.component.css']
})
export class AlertStatusComponent implements OnInit, OnDestroy {

  changesObserver: Subscription;

  constructor(private activatedRoute: ActivatedRoute, private firestore: AngularFirestore) { }

  alert: AlertResponders;

  ngOnInit(): void {
    const alertId = this.activatedRoute.snapshot.params.alertId;

    this.changesObserver = this.firestore.collection('alertResponders').doc<AlertResponders>(alertId).snapshotChanges().subscribe((res) => {
      this.alert = res.payload.data();
    });
  }

  ngOnDestroy() {
    this.changesObserver.unsubscribe();
  }

}
