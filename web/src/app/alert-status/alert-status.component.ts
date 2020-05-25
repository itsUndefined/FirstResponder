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

  pendingCount: number;
  rejectCount: number;
  tooFarCount: number;
  awaitingCount: number;
  ignoredCount: number;
  acceptedCount: number;

  ngOnInit(): void {
    const alertId = this.activatedRoute.snapshot.params.alertId;

    this.changesObserver = this.firestore.collection('alertResponders').doc<AlertResponders>(alertId).snapshotChanges().subscribe((res) => {
      this.alert = res.payload.data();
      console.log(this.alert);
      if (!this.alert) {
        return;
      }
      this.pendingCount = this.getStatusCount('pending_location');
      this.acceptedCount = this.getStatusCount('accepted');
      this.rejectCount = this.getStatusCount('rejected');
      this.tooFarCount = this.getStatusCount('too_far');
      this.awaitingCount = this.getStatusCount('awaiting');
      this.ignoredCount = this.getStatusCount('ignored');
    });
  }

  getStatusCount(type: 'accepted' | 'too_far' | 'rejected' | 'awaiting' | 'pending_location' | 'ignored') {
    let count = 0;
    Object.keys(this.alert.respondersStatus).forEach(responder => {
      if (this.alert.respondersStatus[responder].status === type) {
        count++;
      }
    });
    return count;
  }

  ngOnDestroy() {
    this.changesObserver.unsubscribe();
  }

}
