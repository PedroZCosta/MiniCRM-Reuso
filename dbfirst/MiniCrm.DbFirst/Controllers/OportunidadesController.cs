using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers
{
    public class OportunidadesController : Controller
    {
        private readonly MiniCrmContext _context;

        public OportunidadesController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: Oportunidades
        public async Task<IActionResult> Index()
        {
            var miniCrmContext = _context.Oportunidades.Include(o => o.IdClienteNavigation).Include(o => o.IdMotivoPerdaNavigation).Include(o => o.IdVendedorNavigation);
            return View(await miniCrmContext.ToListAsync());
        }

        // GET: Oportunidades/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var oportunidade = await _context.Oportunidades
                .Include(o => o.IdClienteNavigation)
                .Include(o => o.IdMotivoPerdaNavigation)
                .Include(o => o.IdVendedorNavigation)
                .FirstOrDefaultAsync(m => m.IdOportunidade == id);
            if (oportunidade == null)
            {
                return NotFound();
            }

            return View(oportunidade);
        }

        // GET: Oportunidades/Create
        public IActionResult Create()
        {
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email");
            ViewData["IdMotivoPerda"] = new SelectList(_context.MotivoPerdas, "IdMotivo", "Descricao");
            ViewData["IdVendedor"] = new SelectList(_context.Usuarios, "IdUsuario", "Email");
            return View();
        }

        // POST: Oportunidades/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("IdOportunidade,AtualizadoEm,CriadoEm,DataPrevista,Etapa,FechadaEm,Titulo,ValorEstimado,IdCliente,IdMotivoPerda,IdVendedor")] Oportunidade oportunidade)
        {
            if (ModelState.IsValid)
            {
                _context.Add(oportunidade);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", oportunidade.IdCliente);
            ViewData["IdMotivoPerda"] = new SelectList(_context.MotivoPerdas, "IdMotivo", "Descricao", oportunidade.IdMotivoPerda);
            ViewData["IdVendedor"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", oportunidade.IdVendedor);
            return View(oportunidade);
        }

        // GET: Oportunidades/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var oportunidade = await _context.Oportunidades.FindAsync(id);
            if (oportunidade == null)
            {
                return NotFound();
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", oportunidade.IdCliente);
            ViewData["IdMotivoPerda"] = new SelectList(_context.MotivoPerdas, "IdMotivo", "Descricao", oportunidade.IdMotivoPerda);
            ViewData["IdVendedor"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", oportunidade.IdVendedor);
            return View(oportunidade);
        }

        // POST: Oportunidades/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("IdOportunidade,AtualizadoEm,CriadoEm,DataPrevista,Etapa,FechadaEm,Titulo,ValorEstimado,IdCliente,IdMotivoPerda,IdVendedor")] Oportunidade oportunidade)
        {
            if (id != oportunidade.IdOportunidade)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(oportunidade);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!OportunidadeExists(oportunidade.IdOportunidade))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            ViewData["IdCliente"] = new SelectList(_context.Clientes, "IdCliente", "Email", oportunidade.IdCliente);
            ViewData["IdMotivoPerda"] = new SelectList(_context.MotivoPerdas, "IdMotivo", "Descricao", oportunidade.IdMotivoPerda);
            ViewData["IdVendedor"] = new SelectList(_context.Usuarios, "IdUsuario", "Email", oportunidade.IdVendedor);
            return View(oportunidade);
        }

        // GET: Oportunidades/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var oportunidade = await _context.Oportunidades
                .Include(o => o.IdClienteNavigation)
                .Include(o => o.IdMotivoPerdaNavigation)
                .Include(o => o.IdVendedorNavigation)
                .FirstOrDefaultAsync(m => m.IdOportunidade == id);
            if (oportunidade == null)
            {
                return NotFound();
            }

            return View(oportunidade);
        }

        // POST: Oportunidades/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var oportunidade = await _context.Oportunidades.FindAsync(id);
            if (oportunidade != null)
            {
                _context.Oportunidades.Remove(oportunidade);
            }

            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool OportunidadeExists(int id)
        {
            return _context.Oportunidades.Any(e => e.IdOportunidade == id);
        }
    }
}
