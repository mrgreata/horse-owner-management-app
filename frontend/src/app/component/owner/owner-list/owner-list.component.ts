// src/app/component/owner/owner-list/owner-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OwnerService } from 'src/app/service/owner.service';
import { OwnerDto } from 'src/app/dto/owner';   // ⬅️ statt Owner


@Component({
  standalone: true,
  selector: 'app-owner-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './owner-list.component.html'
})
export class OwnerListComponent implements OnInit {
  owners: OwnerDto[] = [];   // ⬅️ DTO vom Backend
  loading = true;

  constructor(private ownerService: OwnerService) {}

  ngOnInit(): void {
    this.ownerService.listAll().subscribe({
      next: o => { this.owners = o; this.loading = false; },
      error: _ => { this.loading = false; }
    });
  }
}
